package com.ikingsnipe.casino.gui;
import com.ikingsnipe.casino.models.CasinoConfig;
import javax.swing.*; import java.awt.*; import java.util.function.Consumer;
public class CasinoGUI extends JFrame {
    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onComplete) {
        setTitle("Elite Casino v13.0"); setSize(300, 200); setLocationRelativeTo(null);
        JPanel p = new JPanel(new GridLayout(3, 1));
        JButton s = new JButton("START"); s.addActionListener(e -> { onComplete.accept(true); dispose(); });
        JButton c = new JButton("CANCEL"); c.addActionListener(e -> { onComplete.accept(false); dispose(); });
        p.add(new JLabel("Elite Titan Casino", SwingConstants.CENTER)); p.add(s); p.add(c);
        add(p);
    }
}
