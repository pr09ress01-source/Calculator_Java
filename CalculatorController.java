// 電卓アプリケーションのコントローラクラス
// ユーザーの入力を受け取り、モデルを更新し、その結果を画面表示に反映
public class CalculatorController {

    // 計算処理と表示内容を管理するモデル
    private final CalculatorModel model;

    // 計算結果を表示する画面
    private final CalculatorFrame view;

    // コントローラクラスを生成し、モデルを初期化して初期表示を行う
    // @param view 表示を担当する画面
    public CalculatorController(CalculatorFrame view) {
        this.model = new CalculatorModel();
        this.view = view;
        updateDisplay();
    }
    
    // 数字ボタンが押されたときの処理を行う
    // 入力された数字をモデルに追加し、表示を更新
    // @param digitCharacter 入力された数字文字
    public void onDigit(char digitCharacter) {
        model.appendDigit(digitCharacter);
        updateDisplay();
    }

    // 小数点ボタンが押されたときの処理を行う
    // モデルに小数点を追加し、表示を更新
    public void onDot() {
        model.appendDot();
        updateDisplay();
    }

    // 演算子ボタンが押されたときの処理を行う
    // 指定された演算子をモデルに設定し、表示を更新
    // @param operator 入力された演算子
    public void onOperator(Operator operator) {
        model.setOperator(operator);
        updateDisplay();
    }

 // イコールボタンが押されたときの処理を行う
 // モデルに計算を実行させ、その結果を画面に反映する
 public void onEquals() {
     model.equalsOp();
     updateDisplay();
 }

    // クリアボタンが押されたときの処理を行う
    // モデルの内容を初期化し、表示を更新
    public void onClear() {
        model.clearAll();
        updateDisplay();
    }

    // モデルが保持している表示文字列を画面に反映
    private void updateDisplay() {
        view.setDisplay(model.getDisplayText());
    }
}