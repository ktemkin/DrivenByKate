// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.mode.device;

import com.bitwig.extension.controller.api.BrowserResultsItemBank;
import com.bitwig.extension.controller.api.PopupBrowser;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.common.mode.BaseMode;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import com.ktemkin.framework.daw.resource.DBKResourceHandler;
import de.mossgrabers.bitwig.framework.daw.BrowserImpl;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.daw.IBrowser;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IBrowserColumnItem;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.graphics.IImage;
import de.mossgrabers.framework.utils.ButtonEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


/**
 * Mode for searching for and adding a device on-controller.
 * This, in theory, is like a BrowserMode, but in reality we do things an entirely different way.
 *
 * @author Kate Temkin
 */
public class DeviceBrowserMode extends BaseMode<IItem> {

    /**
     * The maximum columns we're wiling to display.
     */
    public final int MAX_COLUMNS = 4;

    /**
     * The root node of the tree of currently-known browser items.
     * Valid only during browser display; and used primarily for memoization.
     */
    protected BrowserModeNode rootNode;

    /**
     * The hash of the device list used to generate the root node.
     * We use this to help avoid spuriously re-generating the rootNode.
     */
    protected int rootNodeDeviceListHash;


    /**
     * True iff we believe we have a selection.
     * Used to make visual decisions without walking the tree.
     */
    protected boolean hasSelection;


    /**
     * Stores how deep our selection currently is.
     * Used to make visual decisions without walking the tree.
     */
    protected int selectionDepth;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public DeviceBrowserMode(final CommonUIControlSurface surface, final IModel model) {
        super("Browser", surface, model);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeactivate() {
        super.onDeactivate();
        this.model.getBrowser().stopBrowsing(false);
    }


    /**
     * Change the value of the last selected column.
     *
     * @param value The change value
     */
    public void changeSelectedColumnValue(final int value) {
        //final int index = 7;
        //this.changeValue(index, value);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobValue(final int index, final int value) {
        if (!this.isKnobTouched(index)) {
            return;
        }

        if (this.increaseKnobMovement()) {
            this.changeValue(index, value);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched) {

        // Make sure that only 1 knob gets changed in browse mode to prevent weird behavior
        if (this.isAnyKnobTouched() && !this.isKnobTouched(index)) {
            return;
        }

        this.setTouchedKnob(index, isTouched);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onFirstRow(final int index, final ButtonEvent event) {
        if (event != ButtonEvent.DOWN) {
            return;
        }

        // If either of the buttons underneath/above a column are pressed,
        // use that to confirm a selection.
        if ((index >> 1) == this.selectionDepth - 1) {

            // Ensure Bitwig's synchronized to our selection...
            this.updateSelection(true);

            // ... and then stop browsing with "commit", which means it accepts our selection.
            this.model.getBrowser().stopBrowsing(true);
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onSecondRow(final int index, final ButtonEvent event) {
        // We use the same logic for first and second row buttons, currently.
        this.onFirstRow(index, event);
    }


    private int getItemsPerScreen() {
        // FIXME: ask the CommonUI class for this
        return this.surface.getBrowserRows();
    }


    /**
     * Displays a non-leaf BrowserModeNode as a column that can be selected from.
     *
     * @param display The display to render to.
     * @param node    The node to be rendered.
     */
    private void displayBrowserModeItem(final IGraphicDisplay display, BrowserModeNode node) {
        // TODO(ktemkin): also include their icons!
        String[] elementNames = node.children.stream().map(BrowserModeNode::getDisplayName).toArray(String[]::new);

        // Create the list of colors for each item.
        IImage[] elementIcons = new IImage[node.children.size()];
        ColorEx[] elementColors = new ColorEx[node.children.size()];
        boolean[] elementBold = new boolean[node.children.size()];
        for (int i = 0; i < elementColors.length; ++i) {
            var isSelected = node.selectedChildIndex == i;

            elementColors[i] = node.children.get(i).getDisplayColor(isSelected);
            elementIcons[i] = node.children.get(i).getDisplayIcon(isSelected);
            elementBold[i] = isSelected;
        }

        display.addDeviceListElement(elementNames, elementIcons, elementColors, elementBold, node.selectedChildIndex, this.getItemsPerScreen());
    }


    /**
     * Displays a placeholder for when there's no results for a column.
     *
     * @param display The display onto which we should render.
     */
    private void displayPlaceholder(IGraphicDisplay display) {
        // TODO(ktemkin): use a more elegant placeholder?
        display.addListElement(new String[]{" "}, new boolean[]{false});
    }


    /**
     * Populates the first item column of our browser display, which breaks items
     * into chunks by their first letter.
     *
     * @param items The items currently visible in our browser window.
     * @return A "root" item that contains all sub-items to be displayed.
     */
    protected BrowserModeNode populateRootBrowserNode(IBrowserColumnItem[] items) {
        TreeMap<String, BrowserModeNode> categories = new TreeMap<>();

        for (var item : items) {

            // Case 0: we don't actually have an item, here -- just an item slot.
            // Skip this and move forward.
            if (!item.doesExist()) {
                continue;
            }

            // Skip any items with no name. What would we even do with them?~
            if (item.getName().length() == 0) {
                continue;
            }

            // For the other cases, we'll need to categorize the item.
            // For the first column, for now, we'll use the first letter of the name.
            var firstChar = item.getName().charAt(0);
            if ((firstChar == '\'') && item.getName().length() > 1) {
                firstChar = item.getName().charAt(1);
            }

            var category = Character.toString(firstChar).toUpperCase();

            // Case 1: We've already seen something that starts with this character.
            // We'll add the item to the existing node.
            if (categories.containsKey(category)) {
                categories.get(category).addChild(item);
            }
            // Case 2: we haven't yet seen something that starts with this character.
            // Create a new node that has a single member for this.
            else {
                var newColumn = BrowserModeNode.newNonLeaf(category, item);
                categories.put(category, newColumn);
            }
        }

        // Finally, squish all of those into another level of split hierarchy, and then return
        // the result as our new root node.
        return this.createSplitAlphabetRootNode(categories);
    }


    /**
     * Takes a collection of "single-letter" BrowserModeNodes and collects them into a root node
     * with an additional level of sorting, which allows selection by _groups_ of letters.
     *
     * @param byLetter A tree-map mapping each single 'letter' to the nodes associated with it.
     * @return The newly created root node.
     */
    protected BrowserModeNode createSplitAlphabetRootNode(TreeMap<String, BrowserModeNode> byLetter) {

        // Create a number of top-level category nodes, which we'll
        //
        // - a node containing 0-9
        // - a node containing A-G
        // - a node containing H-L
        // - a node containing M-R
        // - a node containing S-Z
        // - a node containing everything else (e.g. symbols)
        var node_AtoG = BrowserModeNode.newEmptyNode("A-G");
        var node_HtoL = BrowserModeNode.newEmptyNode("H-L");
        var node_MtoR = BrowserModeNode.newEmptyNode("M-R");
        var node_StoZ = BrowserModeNode.newEmptyNode("S-Z");
        var node_0to9 = BrowserModeNode.newEmptyNode("0-9");
        var node_other = BrowserModeNode.newEmptyNode("symbols");
        var allNodes = List.of(node_0to9, node_AtoG, node_HtoL, node_MtoR, node_StoZ, node_other);

        // Go through each byLetter node, and place it into the appropriate "folder".
        byLetter.forEach((letterString, node) -> {

            // Convert our single-character-string back into a char.
            char letter = letterString.charAt(0);
            if ((letter == '\'') && node.name.length() > 1) {
                letter = node.name.charAt(1);
            }

            // Figure out which folder we want to put our node into.
            BrowserModeNode targetNode;
            if ((letter >= '0') && (letter <= '9')) {
                targetNode = node_0to9;
            } else if ((letter >= 'A') && (letter <= 'G')) {
                targetNode = node_AtoG;
            } else if ((letter >= 'H') && (letter <= 'L')) {
                targetNode = node_HtoL;
            } else if ((letter >= 'M') && (letter <= 'R')) {
                targetNode = node_MtoR;
            } else if ((letter >= 'S') && (letter <= 'Z')) {
                targetNode = node_StoZ;
            } else {
                targetNode = node_other;
            }

            // If this happens to be a collection of size one, skip adding a "subfolder",
            // and add the item directly.
            BrowserModeNode child;
            if (node.children.size() == 1) {
                child = node.children.get(0);
            }
            // Otherwise, create a folder.
            else {
                child = node;
            }

            child.optimizeForDisplay();
            targetNode.addChild(child);

        });

        // Finally, squish each of our "folder" nodes into a top-level root node.
        var rootNode = BrowserModeNode.newEmptyNode("[root node]");
        allNodes.forEach((node) -> {
            // Optimization: only add non-empty nodes.
            if (!node.children.isEmpty()) {
                rootNode.addChild(node);
            }
        });

        // ... and mark our new root node as optimized for display, as well.
        return rootNode;
    }


    /**
     * Generates a hash code that uniquely identifies a set of device names,
     * including their name, order, and "doesExist".
     *
     * @param devices The list of devices to include in the hash.
     * @return A Java hashCode()-alike hash.
     */
    private int hashDeviceSet(IBrowserColumnItem[] devices) {
        int hash = 0;

        // Hash together the strings of all the devices that exist.
        for (var device : devices) {
            if (device.doesExist()) {
                hash ^= device.getName().hashCode();
            }
        }

        return hash;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display) {
        IBrowser browser = this.model.getBrowser();
        if (!browser.isActive()) {
            return;
        }

        // First, we'll need to get a list of the current device display list,
        // which we'll use to generate our tree-view.
        var devices = browser.getResultColumnItems();
        var deviceHash = this.hashDeviceSet(devices);

        // If our device list doesn't match the hash of the device list we used
        // to create our root node, re-create it.
        if ((this.rootNode == null) || (deviceHash != rootNodeDeviceListHash)) {
            this.rootNode = this.populateRootBrowserNode(devices);
            this.rootNodeDeviceListHash = deviceHash;
            this.updateSelection(false);
        }

        //
        // Our display paradigm uses a collection of nested columns,
        // where each column adds more specificity until we find the device we're looking for.
        //
        var selectedNode = this.rootNode;
        for (int i = 0; i < MAX_COLUMNS; ++i) {
            // If we still have a node for this one, draw the relevant column.
            if (selectedNode != null) {
                this.displayBrowserModeItem(display, selectedNode);
                selectedNode = selectedNode.getSelectedChild();
            } else {
                this.displayPlaceholder(display);
            }
        }
    }


    private void changeValue(final int index, final int value) {
        int speed = this.model.getValueChanger().calcSteppedKnobChange(value);
        final boolean direction = speed > 0;
        if (this.surface.isShiftPressed()) {
            speed = speed * 4;
        }

        speed = Math.abs(speed);
        if (direction) {
            this.selectNext(index, speed);
        } else {
            this.selectPrevious(index, speed);
        }
    }


    private BrowserModeNode getNodeForKnob(int index) {
        // Figure out which column this knob is associated with...
        int column = index >> 1;

        // ... and try to go that deep into our column-tree.
        var selectedNode = this.rootNode;
        for (int i = 0; i < column; ++i) {

            selectedNode = selectedNode.getSelectedChild();
            if (selectedNode == null) {
                return null;
            }
        }


        return selectedNode;
    }

    /**
     * Attempts to use evil reflection techniques to get Bitwig's low-level PopupBrowser.
     *
     * @return The PopupBrowser that was alchemized out of the DBM object.
     */
    private PopupBrowser getBitwigInternalBrowser() {
        var browser = (BrowserImpl) this.model.getBrowser();

        // Get the inner field that contains our actual popup browser implementation.
        try {
            Field innerField = browser.getClass().getDeclaredField("browser");
            innerField.setAccessible(true);

            return (PopupBrowser) innerField.get(browser);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }


    /**
     * Attempts to use evil reflection techniques to get Bitwig's low-level PopupBrowser.
     *
     * @return The PopupBrowser that was alchemized out of the DBM object.
     */
    private BrowserResultsItemBank getBitwigInternalResultBank() {
        var browser = (BrowserImpl) this.model.getBrowser();

        // Get the inner field that contains our actual popup browser implementation.
        try {
            Field innerField = browser.getClass().getDeclaredField("resultsItemBank");
            innerField.setAccessible(true);

            return (BrowserResultsItemBank) innerField.get(browser);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }


    /**
     * Updates the metadata regarding our selection.
     */
    private void updateSelection(boolean sendToBitwig) {
        var selectedItem = this.getSelectedItem();

        // If we're sending to Bitwig, do our evil dealio.
        if (sendToBitwig) {
            var bank = this.getBitwigInternalResultBank();
            if ((bank != null) && (selectedItem != null)) {
                var bankItem = bank.getItemAt(selectedItem.getIndex());
                bankItem.isSelected().set(true);
            }
        }

        // Update whether we have a selection.
        this.hasSelection = (selectedItem != null);
        this.selectionDepth = this.getSelectionDepth();
    }


    /**
     * @return The currently selected item, or NULL if none is avaiilable.
     */
    private IBrowserColumnItem getSelectedItem() {
        var browser = this.model.getBrowser();
        var node = this.getSelectedNode();

        // If we don't have a selected node, return NULL.
        if (node == null) {
            return null;
        }

        // If we do, search for the item that matches its name.
        var potentialItems = browser.getResultColumnItems();
        for (var item : potentialItems) {
            if (item.getName().equals(node.name)) {
                return item;
            }
        }

        return null;
    }


    /**
     * @return The currently selected item, or NULL if none is avaiilable.
     */
    private BrowserModeNode getSelectedNode() {
        BrowserModeNode node;

        for (node = this.rootNode; node.hasChildren(); node = node.getSelectedChild()) ;

        // If we couldn't find anything _concrete_, abort.
        if (node == this.rootNode) {
            return null;
        }

        return node;
    }


    /**
     * @return The depth of the given selection; i.e. which column has our selection in it.
     */
    private int getSelectionDepth() {
        BrowserModeNode node;
        int depth = 0;

        for (node = this.rootNode; node.hasChildren(); node = node.getSelectedChild()) {
            ++depth;
        }

        return depth;

    }


    private void selectPrevious(int index, int repeat) {
        var column = this.getNodeForKnob(index);
        if (column == null) {
            return;
        }

        // FIXME: do this to the node for the relevant _column_
        for (int i = 0; i < repeat; ++i) {
            column.moveToPrev();
        }

        this.updateSelection(false);
    }


    private void selectNext(int index, int repeat) {
        var column = this.getNodeForKnob(index);
        if (column == null) {
            return;
        }

        // FIXME: do this to the node for the relevant _column_
        for (int i = 0; i < repeat; ++i) {
            column.moveToNext();
        }

        this.updateSelection(false);
    }


    @Override
    public int getButtonColor(ButtonID button) {
        final var white = this.getColorManager().getDeviceColor(ColorEx.WHITE);
        final var black = this.getColorManager().getDeviceColor(ColorEx.BLACK);
        final var blue  = this.getColorManager().getDeviceColor(ColorEx.CYAN);

        return switch (button) {
            case SELECT ->
                    this.hasSelection ? white : black;
            case ROW1_1, ROW2_1, ROW1_2, ROW2_2 ->
                    (this.selectionDepth == 1) ? blue : black;
            case ROW1_3, ROW2_3, ROW1_4, ROW2_4 ->
                    (this.selectionDepth == 2) ? blue : black;
            case ROW1_5, ROW2_5, ROW1_6, ROW2_6 ->
                    (this.selectionDepth == 3) ? blue : black;
            case ROW1_7, ROW2_7, ROW1_8, ROW2_8 ->
                    (this.selectionDepth == 4) ? blue : black;
            default -> super.getButtonColor(button);
        };


    }


    /**
     * Class representing an item in our browser view.
     */
    protected static class BrowserModeNode {

        /**
         * Icon used for _selected_ nodes that represent folders, e.g. nodes with children.
         */
        public static final IImage ICON_FOLDER_OPEN = DBKResourceHandler.getSVGImage("browser/folder_open.svg");

        /**
         * Icon used for nodes that represent folders, e.g. nodes with children.
         */
        public static final IImage ICON_FOLDER_CLOSED = DBKResourceHandler.getSVGImage("browser/folder_closed.svg");

        /**
         * Icon used for nodes that represent concrete devices.
         */
        public static final IImage ICON_DEVICE = DBKResourceHandler.getSVGImage("browser/kind_device.svg");

        /**
         * The string name associated with this item.
         */
        public String name;

        /**
         * The index of the item in the results list, or -1 if this is not a real item.
         */
        public int index = -1;

        /**
         * The list of items to be displayed in this column.
         */
        public List<BrowserModeNode> children;

        /**
         * The index of the selected child, if any is selected.
         * Meaningless in leaf nodes.
         */
        public int selectedChildIndex = 0;

        /**
         * Creates a new non-leaf BrowserModeItem.
         *
         * @param name      The name of this item.
         * @param firstItem The first child to be added. Will be treated as a leaf node.
         * @return The newly created node.
         */
        public static BrowserModeNode newNonLeaf(String name, IBrowserColumnItem firstItem) {
            var newItem = new BrowserModeNode();

            newItem.name = name;
            newItem.children = new ArrayList<>();

            newItem.children.add(BrowserModeNode.newLeaf(firstItem));

            return newItem;
        }


        /**
         * Creates a new non-leaf BrowserModeItem.
         *
         * @param name      The name of this item.
         * @param firstItem The first child to be added. Will be treated as a leaf node.
         * @return The newly created node.
         */
        public static BrowserModeNode newNonLeaf(String name, BrowserModeNode firstItem) {
            var newItem = new BrowserModeNode();

            newItem.name = name;
            newItem.children = new ArrayList<>();

            newItem.children.add(firstItem);

            return newItem;
        }


        /**
         * Creates a new leaf BrowserModeItem.
         *
         * @param item The actual BrowserColumnItem that this browser item represents.
         * @return The newly created leaf node.
         */
        public static BrowserModeNode newLeaf(IBrowserColumnItem item) {
            var newItem = new BrowserModeNode();

            newItem.name = item.getName();
            newItem.children = new ArrayList<>();
            newItem.index = item.getIndex();

            return newItem;
        }


        /**
         * Creates a new empty BrowserModeItem, intended to be made into a non-leaf node.
         *
         * @param name The name to give this collection.
         * @return The newly created leaf node.
         */
        public static BrowserModeNode newEmptyNode(String name) {
            var newItem = new BrowserModeNode();

            newItem.name = name;
            newItem.children = new ArrayList<>();

            return newItem;
        }


        /**
         * Adds a new child to the given non-leaf node.
         *
         * @param item The item to be added.
         */
        public void addChild(BrowserModeNode item) {
            this.children.add(item);
        }


        /**
         * Adds a new child to the given non-leaf node.
         *
         * @param item The item to be added.
         */
        public void addChild(IBrowserColumnItem item) {
            this.addChild(BrowserModeNode.newLeaf(item));
        }


        /**
         * Moves the selection to the next item, if possible.
         */
        public void moveToNext() {
            // If we still have values after this one to select, do so.
            if ((this.selectedChildIndex + 1) < this.children.size()) {
                this.selectedChildIndex += 1;
            }
        }


        /**
         * Moves the selection to the previous item, if possible.
         */
        public void moveToPrev() {
            // If we still have values after this one to select, do so.
            if (this.selectedChildIndex > 0) {
                this.selectedChildIndex -= 1;
            }
        }


        /**
         * @return The node for the currently selected child.
         */
        public BrowserModeNode getSelectedChild() {
            // If we don't have any children, return NULL.
            if (this.children.isEmpty()) {
                return null;
            }

            // Otherwise, return the selected child.
            return this.children.get(this.selectedChildIndex);
        }


        /**
         * @return The name, with any stylization required.
         */
        public String getDisplayName() {
            return this.name;
        }


        /**
         * Performs an in-place "display optimization", which groups children
         * by e.g. repeated first words.
         */
        public void optimizeForDisplay() {

            TreeMap<String, BrowserModeNode> categories = new TreeMap<>();

            for (var child : this.children) {

                // We'll categorize each item by its first word, so split that out.
                var words = child.name.split(" ");
                var firstWord = words[0];

                // Case 1: We've already seen something that starts with this character.
                // We'll add the item to the existing node.
                if (categories.containsKey(firstWord)) {
                    categories.get(firstWord).addChild(child);
                }
                // Case 2: we haven't yet seen something that starts with this character.
                // Create a new node that has a single member for this.
                else {
                    var newColumn = BrowserModeNode.newNonLeaf(firstWord, child);
                    categories.put(firstWord, newColumn);
                }
            }

            // Finally, take all of the new nodes we've created, and make those our children.
            this.children = new ArrayList<>();
            categories.forEach((firstWord, node) -> this.addChild((node.children.size() == 1) ? node.children.get(0) : node));
        }

        /**
         * @return The color that should be used to display this node.
         */
        public ColorEx getDisplayColor(boolean isSelected) {
            // FIXME(ktemkin): this should depend on the color manager
            return isSelected ? ColorEx.SKY_BLUE : ColorEx.GRAY;
        }


        /**
         * @return The icon associated with this node.
         */
        public IImage getDisplayIcon(boolean isSelected) {
            if (this.children.isEmpty()) {
                return ICON_DEVICE;
            } else {
                return isSelected ? ICON_FOLDER_OPEN : ICON_FOLDER_CLOSED;
            }
        }


        /**
         * @return True iff this node has children.
         */
        public boolean hasChildren() {
            return !this.children.isEmpty();
        }
    }

}