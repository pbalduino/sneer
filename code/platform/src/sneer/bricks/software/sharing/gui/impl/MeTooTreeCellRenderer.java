package sneer.bricks.software.sharing.gui.impl;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

class MeTooTreeCellRenderer extends DefaultTreeCellRenderer {

@Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, 
			boolean expanded, boolean leaf, int row, boolean hasFocus_) {
	  
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus_);
    
    ImageIcon icon = ((AbstractTreeNodeWrapper<?>) value).getIcon();
    
    if (icon != null) 
      setIcon(icon);

    return this;
  }
}