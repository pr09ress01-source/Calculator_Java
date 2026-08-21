import javax.swing.SwingUtilities;

// 電卓アプリケーションを起動するメインクラス
// ビューとコントローラを生成して関連付けし、電卓画面を表示する
public class CalculatorApp {

	//アプリケーションを起動
    // Swingの画面生成処理はイベントディスパッチスレッド上で実行
    // @param args コマンドライン引数
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        	            
            // 1. 電卓画面を表示するビューを生成
            CalculatorFrame view = new CalculatorFrame();
            
            // 2. モデルとビューの橋渡しをするコントローラーを生成
            CalculatorController controller = new CalculatorController(view);
            
            // 3. view にコントローラーを関連付ける
            //    ボタン操作に応じてコントローラーの各メソッドが呼び出される
            view.bindController(controller);
            
            // 4. 電卓画面を表示する
            view.setVisible(true);
        });
    }
}