import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


import net.java.frej.*;


public class FrejDemo extends Applet implements ActionListener {
 
    private String defPattern = "{(^Start,Run)~A, (?Demo)|, Applet~B}|Test_$B_is_$A-ing";
    private String defInput = "ran appelt";

    private Button btnExact, btnStart, btnSubstr, btnDemo;
    private TextArea txtRegex, txtInput, txtAnswer;
    private List<Pos> poses = new java.util.LinkedList<Pos>();
   
    private List<String[]> lstrExamples = Arrays.asList(new String[][] {
        {"{=frej,(?test)}", "frej test", "fresh test", "test", "toast frej"},
        {"[^(reg*,expr*),regexp]", "regular expression", "regolar suppression", "regexpr"},
        {"[(^java,jre), (#5:7), compat*]", "jre 5 ocmpatible", "javac 7 compatibility", "java 4 compatible"}});
    	
    
    private static class Pos {
        public Component c;
        public float x, y, w, h;
        public Pos(Component c, double x, double y, double w, double h) {
            this.c = c;
            this.x = (float) x; this.y = (float) y;
            this.w = (float) w; this.h = (float) h;
        } // Pos
    } // class Pos
    
    
    public FrejDemo() {
        super();

        setLayout(null);
        
        
        addComponentListener(new ComponentAdapter(){
            @Override    
            public void componentResized(ComponentEvent e) { FrejDemo.this.componentResized(); }
        }); // addComponentListener
        
        poses.add(new Pos(new Label("Pattern:"), 0.1, 0.05, 0.2, 0.1));
        poses.add(new Pos(btnDemo = new Button("Demo"),0.7, 0.04, 0.2, 0.1 ));
        poses.add(new Pos(txtRegex = new TextArea(defPattern), 0.1, 0.15, 0.8, 0.2));
        poses.add(new Pos(new Label("Input text:"), 0.1, 0.35, 0.8, 0.1));
        poses.add(new Pos(txtInput = new TextArea(defInput), 0.1, 0.45, 0.8, 0.15));
        poses.add(new Pos(btnExact = new Button("Exact"), 0.1, 0.65, 0.2, 0.1));
        poses.add(new Pos(btnStart = new Button("Start"), 0.4, 0.65, 0.2, 0.1));
        poses.add(new Pos(btnSubstr = new Button("SubStr"), 0.7, 0.65, 0.2, 0.1));
        poses.add(new Pos(new Label("Answer:"), 0.1, 0.75, 0.8, 0.1));
        poses.add(new Pos(txtAnswer = new TextArea(), 0.1, 0.85, 0.8, 0.1));
        
        for (Pos p : poses) {
            add(p.c);
        } // for

        btnExact.addActionListener(this);
        btnStart.addActionListener(this);
        btnSubstr.addActionListener(this);
        btnDemo.addActionListener(this);
                
    } // Applet


    @Override
	public void actionPerformed(ActionEvent evt) {
        
        try {
            boolean b;
            Regex r = new Regex(txtRegex.getText());

            if(evt.getSource() == btnDemo){
            	//random generation
            	setTextareaRandomly(lstrExamples);
            	b = true;
            }
            else if (evt.getSource() == btnExact) {
                b = r.match(txtInput.getText());
            } else if (evt.getSource() == btnStart) {
                b = r.matchFromStart(txtInput.getText());
            } else {
                b = r.presentInSequence(txtInput.getText()) >= 0;
            } // else
            
            if (b) {
                txtAnswer.setText(r.getReplacement());
            } else {
                txtAnswer.setText("// Not matched!");
            } // else
        } catch (Exception e) {
            txtAnswer.setText("// Exception!");
            return;
        } // catch

    } // actionPerformed
    
    
    /**
     * set First element of this array 
	 * into upper textarea (pattern). One of other elements (chosen randomly
	 * sets into middle textarea ("Input text").
     * @param lstrRegex - List of arrays that include regular expressions string
     */
    public void setTextareaRandomly(List<String[]> lstrRegex) {
		Random rGenerator = new Random();
		int nRand = rGenerator.nextInt(lstrRegex.size());
		
		/*choose random array String from the List*/
		String[] strRegex = lstrRegex.get(nRand);
		
		/*Set the first textArea with a first String from the List*/
		txtRegex.setText(strRegex[0]);
		
		/*Generate random number but not the first one as it already used*/
		nRand = rGenerator.nextInt(strRegex.length - 1);
		
		/*Set the second textArea with a random String from the List*/
		txtInput.setText(strRegex[nRand + 1]);
    } // setTextareaRandomly


    public void componentResized() {
        int w = getWidth();
        int h = getHeight();

        for (Pos p : poses) {
            p.c.setLocation((int) (w * p.x), (int) (h * p.y));
            p.c.setSize((int) (w * p.w), (int) (h * p.h));
        } // for

        btnExact.requestFocus();

    } // componentResized


} // Applet
