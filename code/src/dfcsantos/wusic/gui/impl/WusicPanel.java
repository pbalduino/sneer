package dfcsantos.wusic.gui.impl;

import static sneer.foundation.environments.Environments.my;
import static dfcsantos.wusic.Wusic.OperatingMode;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle;

import sneer.bricks.skin.widgets.reactive.ReactiveWidgetFactory;
import dfcsantos.wusic.Wusic;

/**
 * 
 * @author daniel
 */
class WusicPanel extends JPanel {

	private static final Wusic Wusic = my(Wusic.class);

    private JButton _meTooButton;
    private JRadioButton _ownTracks;
    private JCheckBox _shuffleMode;
    private JButton _noWayButton;
    private JButton _pauseButton;
    private JLabel _trackLabel;
    private JLabel _trackTime;
    private JButton _skipButton;
    private JButton _stopButton;
    private JRadioButton _tracksFromPeers;
    private ButtonGroup _tracksSource;

	{
		_trackLabel = my(ReactiveWidgetFactory.class).newLabel(Wusic.playingTrackName()).getMainWidget();
		_trackTime = my(ReactiveWidgetFactory.class).newLabel(Wusic.playingTrackTime()).getMainWidget();
        _tracksSource = new ButtonGroup();
        _ownTracks = new JRadioButton();
        _shuffleMode = new JCheckBox();
        _tracksFromPeers = new JRadioButton();
        _pauseButton = new JButton();
        _skipButton = new JButton();
        _stopButton = new JButton();
        _meTooButton = new JButton();
        _noWayButton = new JButton();

        _tracksSource.add(_ownTracks);
        _ownTracks.setSelected(true);
        _ownTracks.setText("Play Own Tracks");
        _ownTracks.setName("ownTracks"); // NOI18N
        _ownTracks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                myTracksActionPerformed();
            }
        });

        _shuffleMode.setText(" Shuffle Mode");
        _shuffleMode.setSelected(false);
        _shuffleMode.addActionListener(new ActionListener() {
        	@Override public void actionPerformed(ActionEvent e) {
				shuffleModeActionPerformed();
			}
		});

        _tracksSource.add(_tracksFromPeers);
        _tracksFromPeers.setText("Play Tracks From Peers");
        _tracksFromPeers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                tracksFromPeersActionPerformed();
            }
        });

        _trackLabel.setFont(new Font("Tahoma", 2, 14));
        _trackTime.setFont(new Font("Tahoma", 2, 14));

        _pauseButton.setText("> / ||");
        _pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pauseButtonActionPerformed();
            }
        });

        _skipButton.setText(">>");
        _skipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                skipButtonActionPerformed();
            }
        });

        _stopButton.setText("Stop");
        _stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopButtonActionPerformed();
            }
        });

        _meTooButton.setText("Me Too :)");
        _meTooButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                meTooButtonActionPerformed();
            }
        });

        _noWayButton.setText("Delete File!");
        _noWayButton.setEnabled(false);
        _noWayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                noWayButtonActionPerformed();
            }
        });

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                	.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                		.addComponent(_trackTime)
                		.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                		.addComponent(_trackLabel))
                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                    	.addComponent(_ownTracks)
                    	.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    	.addComponent(_shuffleMode)
               		.addContainerGap())
                    .addComponent(_tracksFromPeers, GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(_pauseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(_skipButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(_stopButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_meTooButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_noWayButton)))
                .addContainerGap())
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
           		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            		.addComponent(_ownTracks)
            		.addComponent(_shuffleMode))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_tracksFromPeers)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    	.addComponent(_trackTime)
                    	.addComponent(_trackLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(_pauseButton)
                    .addComponent(_skipButton)
                    .addComponent(_stopButton)
                    .addComponent(_meTooButton)
                    .addComponent(_noWayButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
	}

	void enableDeleteFileActionPerformed(boolean enabled) {
		_noWayButton.setEnabled(enabled);
	}

	private void pauseButtonActionPerformed() {                                            
    	Wusic.pauseResume();
    }                                           

    private void skipButtonActionPerformed() {
        Wusic.skip();
    }

    private void stopButtonActionPerformed() {
        Wusic.stop();
    }

    private void meTooButtonActionPerformed() {
        Wusic.meToo();
    }

    private void noWayButtonActionPerformed() {
        Wusic.noWay();
    }

    private void myTracksActionPerformed() {
        Wusic.setOperatingMode(OperatingMode.OWN);
    }

    private void tracksFromPeersActionPerformed() {
        Wusic.setOperatingMode(OperatingMode.PEERS);
    }

    private void shuffleModeActionPerformed() {
    	Wusic.setShuffleMode(_shuffleMode.isSelected());
	}

}