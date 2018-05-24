package ru.nsu.tester.gui

import org.abego.treelayout.util.DefaultTreeForTreeLayout
import org.abego.treelayout.util.DefaultConfiguration
import org.abego.treelayout.TreeForTreeLayout
import org.abego.treelayout.TreeLayout
import ru.nsu.tester.comparison.wrapper.TreeWrapper
import java.awt.*
import java.awt.geom.Rectangle2D
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.UIManager

private const val GAP_BETWEEN_LEVELS = 50.0
private const val GAP_BETWEEN_NODES = 20.0

private const val STROKE_SIZE = 1.5
private const val ARC_SIZE = 10
private const val OFFSET = 5

class TreePanel(tree: TreeWrapper) : JPanel() {
    var treeLayout: TreeLayout<TreeWrapper>

    init {
        font = Font("Tahoma", Font.PLAIN, 13)
        val treeForTreeLayout = DefaultTreeForTreeLayout(tree)
        fillTreeForTreeLayout(treeForTreeLayout, tree)

        val configuration = DefaultConfiguration<TreeWrapper>(GAP_BETWEEN_LEVELS, GAP_BETWEEN_NODES)
        treeLayout = TreeLayout(treeForTreeLayout, MyNodeExtentProvider(), configuration)

        preferredSize = treeLayout.bounds.bounds.size
        border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY)
    }

    private fun fillTreeForTreeLayout(treeForTreeLayout: DefaultTreeForTreeLayout<TreeWrapper>, tree: TreeWrapper) {
        for (i in 0 until tree.childrenCount()) {
            val child = tree.getChild(i)
            if (!child.isValuable()) continue
            treeForTreeLayout.addChild(tree, child)
            fillTreeForTreeLayout(treeForTreeLayout, child)
        }
    }

    private fun getTree(): TreeForTreeLayout<TreeWrapper> {
        return treeLayout.tree
    }

    private fun getChildren(parent: TreeWrapper): Iterable<TreeWrapper> {
        return getTree().getChildren(parent)
    }

    private fun getBoundsOfNode(node: TreeWrapper): Rectangle2D.Double {
        return treeLayout.nodeBounds[node]!!
    }

    private fun paintEdges(g: Graphics?, parent: TreeWrapper) {
        val g2 = g as Graphics2D
        g2.stroke = BasicStroke(STROKE_SIZE.toFloat())
        if (!getTree().isLeaf(parent)) {
            val b1 = getBoundsOfNode(parent)
            for (child in getChildren(parent)) {
                val b2 = getBoundsOfNode(child)
                g2.drawLine(b1.centerX.toInt() + OFFSET,
                        b1.centerY.toInt() + OFFSET,
                        b2.centerX.toInt() + OFFSET,
                        b2.centerY.toInt() + OFFSET)

                paintEdges(g, child)
            }
        }
    }

    private fun paintBox(g: Graphics, tree: TreeWrapper) {
        var text = tree.getName()
        var textColor = Color.BLACK
        if (tree.childrenCount() == 0) {
            text = tree.getText()
            textColor = Color.LIGHT_GRAY
        }

        g.color = UIManager.getColor ("Panel.background")
        val box = getBoundsOfNode(tree)
        val metrics = g.getFontMetrics(font)
        g.fillRoundRect(box.x.toInt() + OFFSET,
                box.y.toInt() + OFFSET,
                box.width.toInt(),
                box.height.toInt(),
                ARC_SIZE,
                ARC_SIZE)

        g.color = textColor
        val x = box.x.toInt() + ARC_SIZE / 2
        val y = box.y.toInt() + metrics.ascent + metrics.leading + 1
        g.drawString(text, x + OFFSET, y + OFFSET)
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        paintEdges(g, getTree().root)
        for (textInBox in treeLayout.nodeBounds.keys) {
            paintBox(g!!, textInBox)
        }
    }
}