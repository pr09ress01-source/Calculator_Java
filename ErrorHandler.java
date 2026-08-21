// 例外をログ出力するクラス
public class ErrorHandler {

    // 捕捉した例外をエラーメッセージとスタックトレース付きで出力
    // @param exception try-catchで捕まえた例外
    public static void handle(Exception exception) {

        // 例外がnullの場合は、詳細不明のエラーとしてメッセージだけ出力する
        if (exception == null) {
            System.err.println("エラーが発生しました。詳細は不明です。");
            return;
        }

        // 例外クラス名とメッセージを標準エラー出力に表示する
        System.err.println("[エラー] "
                + exception.getClass().getSimpleName()
                + ": "
                + exception.getMessage());

        // スタックトレースを出力して、発生箇所を追跡できるようにする
        exception.printStackTrace();
    }
}