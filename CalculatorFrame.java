import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// 電卓アプリケーションの画面を担当するクラス
// MVC におけるビューとして、表示ラベルとキーパッドを管理
public class CalculatorFrame extends JFrame {

    // 計算式や計算結果を表示するラベルです。
    private JLabel displayLabel = new JLabel("0");

    // 数字ボタンや演算子ボタンを配置するキーパッド用パネル
    private JPanel keypadPanel;

    // 各数字(0~9)の入力ボタン
    private JButton zeroButton = new JButton("0");
    private JButton oneButton = new JButton("1");
    private JButton twoButton = new JButton("2");
    private JButton threeButton = new JButton("3");
    private JButton fourButton = new JButton("4");
    private JButton fiveButton = new JButton("5");
    private JButton sixButton = new JButton("6");
    private JButton sevenButton = new JButton("7");
    private JButton eightButton = new JButton("8");
    private JButton nineButton = new JButton("9");

    // 小数点を入力するボタン
    private JButton decimalPointButton = new JButton(".");

    // 演算子を入力するボタン
    private JButton additionButton = new JButton("+");
    private JButton subtractionButton = new JButton("−");
    private JButton multiplicationButton = new JButton("×");
    private JButton divisionButton = new JButton("÷");
    private JButton equalsButton = new JButton("=");
    private JButton clearButton = new JButton("C");

    // 電卓ウィンドウを初期化するコンストラクタ
    // 画面の基本設定と表示ラベル、キーパッドの配置を行う
    public CalculatorFrame() {
    	
    	// ウィンドウのタイトルバーに表示する文字列を設定する
        setTitle("Calculator");
        
        // ウィンドウの閉じるボタンが押されたときに、アプリケーション全体を終了する
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // ウィンドウの大きさを幅 330、高さ 460 に設定する
        setSize(330, 460);
        
        // ウィンドウを画面中央に表示する
        setLocationRelativeTo(null);
        
        // このフレーム全体のレイアウトをBorderLayout
        // 横方向・縦方向の部品のすき間を5, 5
        setLayout(new BorderLayout(5, 5));
        
        // 表示ラベルの文字を右寄せ
        displayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // 表示ラベルのフォントを設定する
        // Monospacedは等幅フォント、Font.BOLDは太字、文字サイズ28
        displayLabel.setFont(new Font("Monospaced", Font.BOLD, 28));
        
        // 表示ラベルのサイズは幅330、高さ60にして計算結果を見やすくする
        displayLabel.setPreferredSize(new Dimension(330, 60));
        
        // 表示ラベルをフレームの上部に配置
        add(displayLabel, BorderLayout.NORTH);
        
        // キーパッド用のパネルを生成
        // GridLayout(5, 4, 5, 5) は 5 行 4 列の表形式に部品を並べる設定
        // 最後の 5, 5 はボタン間の横方向・縦方向のすき間
        keypadPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        
        // 1行目：7, 8, 9, ÷ の順にボタンを配置
        keypadPanel.add(sevenButton);
        keypadPanel.add(eightButton);
        keypadPanel.add(nineButton);
        keypadPanel.add(divisionButton);
        
        // 2行目：4, 5, 6, × の順にボタンを配置
        keypadPanel.add(fourButton);
        keypadPanel.add(fiveButton);
        keypadPanel.add(sixButton);
        keypadPanel.add(multiplicationButton);
        
        // 3行目：1, 2, 3, − の順にボタンを配置
        keypadPanel.add(oneButton);
        keypadPanel.add(twoButton);
        keypadPanel.add(threeButton);
        keypadPanel.add(subtractionButton);
        
        // 4行目：0, 小数点, =, + の順にボタンを配置
        keypadPanel.add(zeroButton);
        keypadPanel.add(decimalPointButton);
        keypadPanel.add(equalsButton);
        keypadPanel.add(additionButton);

        // 5行目の先頭にクリアボタンを配置
        keypadPanel.add(clearButton);
        
        // 残り3マスは空欄ボタンを配置
        keypadPanel.add(new JButton());
        keypadPanel.add(new JButton());
        keypadPanel.add(new JButton());
        
        // キーパッド全体をフレーム中央に配置
        add(keypadPanel, BorderLayout.CENTER);
    }

    // 画面に表示する文字列を設定
    // @param text 表示ラベルに設定する文字列
    public void setDisplay(String text) {
        displayLabel.setText(text);
    }

    // 画面上の各ボタンにコントローラを関連付ける
    // @param c ボタン操作を処理するコントローラ
    public void bindController(CalculatorController c) {
        zeroButton.addActionListener(event -> c.onDigit('0'));
        oneButton.addActionListener(event -> c.onDigit('1'));
        twoButton.addActionListener(event -> c.onDigit('2'));
        threeButton.addActionListener(event -> c.onDigit('3'));
        fourButton.addActionListener(event -> c.onDigit('4'));
        fiveButton.addActionListener(event -> c.onDigit('5'));
        sixButton.addActionListener(event -> c.onDigit('6'));
        sevenButton.addActionListener(event -> c.onDigit('7'));
        eightButton.addActionListener(event -> c.onDigit('8'));
        nineButton.addActionListener(event -> c.onDigit('9'));

        decimalPointButton.addActionListener(event -> c.onDot());

        additionButton.addActionListener(event -> c.onOperator(Operator.ADD));
        subtractionButton.addActionListener(event -> c.onOperator(Operator.SUB));
        multiplicationButton.addActionListener(event -> c.onOperator(Operator.MUL));
        divisionButton.addActionListener(event -> c.onOperator(Operator.DIV));

        equalsButton.addActionListener(event -> c.onEquals());
        clearButton.addActionListener(event -> c.onClear());
    }
}