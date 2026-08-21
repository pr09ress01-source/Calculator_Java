import java.math.BigDecimal;
import java.math.RoundingMode;

//表示用の書式処理をまとめたユーティリティクラス
public class FormatterUtil {

	// 電卓で表示・扱う最大桁数（8桁）を表す定数
    private static final int MAX_DIGITS = 8;

    // インスタンス化させないための private コンストラクタ（ユーティリティクラス専用の作り方）
    private FormatterUtil() {
    }

    // 計算結果を画面に表示するための文字列に整形する
    public static String formatResultForDisplay(BigDecimal value) {
    	
    	// null（値がない）場合は0として表示する
        if (value == null) {
            return "0";
        }

        // 値がちょうど0の場合も、0として表示する
        if (value.signum() == 0) {
            return "0";
        }

        // 末尾の不要な0を削って（例：1.2300 → 1.23）、その結果を文字列にする
        BigDecimal stripped = value.stripTrailingZeros();  
        String plain = stripped.toPlainString();

        // 符号を除いた絶対値部分の文字列を作る（先頭が '-' なら外す）
        String absPlain = plain.startsWith("-") ? plain.substring(1) : plain;
        
        // 小数点を取り除いて、数字だけの文字列にする
        String digitsOnly = absPlain.replace(".", "");

        // 全体の数字の個数が最大桁数を超える場合は、通常表示ではなく指数表記にする
        if (digitsOnly.length() > MAX_DIGITS) {
            return toScientificString(value);
        }

        // 0.〜で始まる小さい値（0.0012 や -0.0003 など）の場合の追加チェック
        if (absPlain.startsWith("0.") || absPlain.startsWith("-0.")) {
            int firstNonZero = firstNonZeroAfterDecimal(absPlain); // 小数点の後で最初に0以外の数字が現れる位置を探す
            
            // 有効な小数部分の桁数と合わせて最大桁数を超える場合は指数表記にする
            if (firstNonZero >= 0 && firstNonZero + countSignificantFractionDigits(absPlain) > MAX_DIGITS) {
                return toScientificString(value);
            }
            
            // 先頭の0.の後に0がたくさん続くような非常に小さい値も指数表記にする
            if (firstNonZero >= 2) {
                return toScientificString(value);
            }
        }
        
        // 上の条件に引っかからない場合は、通常の文字列表現をそのまま返す
        return plain;
    }

    // BigDecimal の値を指数表記（例：1.23e4 のような形）に変換して文字列として返す
    private static String toScientificString(BigDecimal value) {
    	
    	// 末尾の不要な 0 を取り除いた値を作る（例：123.4500 → 123.45）
        BigDecimal stripped = value.stripTrailingZeros(); 

        // 値が0の場合は指数表記ではなく単純に0を返す
        if (stripped.signum() == 0) {
            return "0";
        }

        // 指数部（10の何乗か）を計算する：precision - scale - 1が桁数から指数を求める式
        int exponent = stripped.precision() - stripped.scale() - 1;

        // 仮数（mantissa）を 1桁目に小数点が来るように移動し、(MAX_DIGITS - 1) 桁に丸めて整形する
        BigDecimal mantissa = stripped.movePointLeft(exponent)
                .setScale(MAX_DIGITS - 1, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        // 仮数の通常表記 + "e" + 指数の形で指数表記の文字列を返す（例："1.2345678e4"）
        return mantissa.toPlainString() + "e" + exponent;
    }

    // 通常表記の文字列 plain について、小数点の後で最初に0以外の数字が現れる位置を調べる
    private static int firstNonZeroAfterDecimal(String plain) {
    	
    	// 小数点の位置を探す（なければ-1を返す）
        int dot = plain.indexOf('.');
        if (dot < 0) {
            return -1;
        }

        // 小数点の直後から末尾まで走査して、最初に0以外の数字が出てくる位置を探す
        for (int i = dot + 1; i < plain.length(); i++) {
            char c = plain.charAt(i);
            if (c != '0') {
                return i - dot - 1; // 小数点から何桁目か（0.00...X の X が何桁目か）を返す
            }
        }
        return -1; // 小数点以下が全部0だった場合は-1を返す（有効な数字がないという意味）
    }

    // 小数点以下に含まれる有効な数字（1〜9）の個数を数える
    private static int countSignificantFractionDigits(String plain) {
    	
    	// 文字列の中から小数点の位置を探す（なければ0を返す）
        int dot = plain.indexOf('.');
        if (dot < 0) {
            return 0;   // 小数点がない場合は小数部分がないので有効桁数は0
        }

        int count = 0;
        
        // 小数点の直後から末尾まで1文字ずつ走査する
        for (int i = dot + 1; i < plain.length(); i++) {
            char c = plain.charAt(i);
            
            // '1'〜'9' の数字だけを「有効な小数桁」として数える（0 は桁に含めない）
            if (c >= '1' && c <= '9') {
                count++;
            }
        }
        return count; // 見つかった有効な小数桁の個数を返す
    }
}