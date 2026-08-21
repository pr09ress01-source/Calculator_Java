import java.math.BigDecimal;
import java.math.RoundingMode;

// 電卓アプリケーションの計算状態と計算処理を管理するモデルクラス
public class CalculatorModel {

    // 左辺の値を保持
    private BigDecimal leftOperand;

    // 現在入力中の数値を文字列として保持
    private final StringBuilder currentInput = new StringBuilder();

    // 画面に表示する計算式や結果を文字列として保持
    private final StringBuilder expression = new StringBuilder();

    // 演算子を保持
    private Operator pendingOp;

    // 現在の入力状態を保持
    private InputState state;

    // 計算結果を表示した直後かどうか
    private boolean resultJustShown;

    // クリア直後かどうか
    private boolean clearedJustNow;

    // 入力できる最大桁数(8桁)
    private static final int maxDigits = 8;

    // モデルを初期化するコンストラクタ
    // 電卓の状態を初期状態に戻す
    public CalculatorModel() {
        clearAll();
        clearedJustNow = false;
    }

    // 電卓の状態を初期状態に戻す
    // 入力中の値、計算式、演算子、状態をすべてリセットする
    public void clearAll() {
        leftOperand = null;        // 左辺の値を未設定状態に戻す
        pendingOp = null;          // 保留中の演算子をクリアして、次に行う計算の予約をリセットする
        currentInput.setLength(0); // 現在入力中の数値をクリアして、入力欄を空にする
        expression.setLength(0);   // まず表示を空にする
        expression.append("0");    // クリア後は0を表示する
        state = InputState.READY;  // 電卓全体の状態を「READY（次の入力待ちの初期状態）」に戻す
        resultJustShown = false;   // 直前に結果を表示したばかり
        clearedJustNow = true;     // 今まさに全クリアをした直後であることを示す
    }

    // 表示内容が空かどうかを調べる
    // @return 表示が空、または0のみならtrue
    public boolean isEmpty() {
        return expression.length() == 0 || "0".contentEquals(expression);
    }

    // 画面に表示する文字列を返す
    // エラー状態や結果表示直後かどうかを見て、適切な文字列を返す
    // @return 表示用文字列
    public String getDisplayText() {
    	
    	// 計算中にエラーが発生して状態がエラーになっていたら、結果の表示は行わずに終了する
        if (state == InputState.ERROR) {
            return "エラー";
        }

        // 計算中にエラーが発生して状態がエラーになっていたら、結果の表示は行わずに終了する
        if (resultJustShown && leftOperand != null) {
            return FormatterUtil.formatResultForDisplay(leftOperand);
        }

        // 計算中にエラーが発生して状態がエラーになっていたら、結果の表示は行わずに終了する
        if (expression.length() == 0) {
            return "0";
        }
        
        // 計算中にエラーが発生して状態がエラーになっていたら、結果の表示は行わずに終了する
        return expression.toString();
    }

    // 表示用文字列を返す
    // getDisplayText() と同じ内容を返す
    // @return 表示用文字列
    public String getText() {
        return getDisplayText();
    }

    // 演算子入力時に、左辺を 0 として計算を開始
    // 先頭で演算子が押された場合の補助処理
    // @param op 入力された演算子
    public void startWithZero(Operator op) {
    	
    	// 演算子が-の時だけ、少し特別な扱いをする分岐
        if (op == null) {
            return;
        }

        expression.setLength(0);  // 式を入れているStringBuilderの中身を全部消して空にする

        if (op == Operator.SUB) {          // 演算子がマイナスのとき専用の初期化
            expression.append("0-");       // 左側に0と−が表示されて次の数の入力待ち
            leftOperand = BigDecimal.ZERO; // 左の値は0
            pendingOp = Operator.SUB;      // 次に使う予定の演算子は引き算
        } else {
            expression.append("0").append(operatorSymbol(op)); // 左側0の後ろに指定された演算記号を表示して、0+や0×のような形の式を作る
            leftOperand = BigDecimal.ZERO; // 左の値は 0 に固定
            pendingOp = op;                // 次に使う予定の演算子を覚えておく
        }

        currentInput.setLength(0);         // 現在入力中の数字部分を空にリセット
        state = InputState.INPUT_OPERATOR; // 今は演算子を入力した直後の状態と記録
        resultJustShown = false;           // 直前に計算結果を表示したわけではない
        clearedJustNow = false;            // クリアした直後でもない
    }
    
    // 押された数字を後ろに追加するためのメソッド
    // @param ch 入力された数字文字
    // @return 追加できた場合はtrue、できなかった場合はfalse
    public boolean appendDigit(char ch) {
    	
    	// 渡された文字が数字かどうかをチェック(演算子や小数点は弾く)
        if (!Character.isDigit(ch)) {
            return false;
        }
        
        // 電卓がエラー状態のときに数字が押された場合、数字を受け付けない
        if (state == InputState.ERROR) {
        	return false;
        }

        // 計算結果を表示した直後に数字が押された場合、新しく計算をやり直せる処理
        if (resultJustShown) {
            clearAll();
            clearedJustNow = false;
        }

        // 今までに入力された数字が何桁あるかを数える
        int digitCount = countDigits(currentInput);
        
        // 最大桁数を超えていたら拒否
        if (digitCount >= maxDigits) {
            return false;
        }

        // 先頭が0のときに別の数値を押下した場合、0と数値を置き換える
        if (currentInput.length() == 1 && currentInput.charAt(0) == '0') {
        	
            if (ch == '0') {
            	currentInput.setLength(0);        // いま入っている0を消して、文字列を空に
            	currentInput.append(ch);          // 新しく押された数字を1文字だけ追加
            } else {
                // 0 以外なら、先頭の 0 を消して新しい数字に置き換える
                currentInput.setLength(0);
                currentInput.append(ch);
            }
            
            syncExpressionWithCurrentInput(); // 式表示用の文字列も、この新しい入力内容と同期
            state = InputState.INPUT_NUMBER;  // 今は数字を入力している途中と記録
            resultJustShown = false;          // 今は計算結果を表示した直後ではないと記録
            clearedJustNow = false;           // 今はクリアした直後でもないと記録
            return true;
        }

        // -0 の状態では次の数字で0を置き換える
        if (currentInput.length() == 2
                && currentInput.charAt(0) == '-'
                && currentInput.charAt(1) == '0') {
            if (ch == '0') {                  // 押された数字がまた  0  の場合は、何もしないで終わり。
                return false;
            }
            currentInput.setLength(1);        // 2文字あった-0のうち、長さを1に切り詰め、文字列は-だけ残る
            currentInput.append(ch);          // さっき削った0の代わりに、新しく押された数字chを1文字追加
            syncExpressionWithCurrentInput(); // 表示用の式も、この新しい-5に合わせて更新
            state = InputState.INPUT_NUMBER;  // 今は数字を入力している途中の状態と記録
            resultJustShown = false;          // 結果を出した直後でもないと記録
            clearedJustNow = false;           // クリアした直後でもないと記録
            return true;                      // 今回の数字入力はうまく処理できたという意味でtrueを返す
        }

        currentInput.append(ch); // 今まで入力されている文字列の後ろに1文字chをくっつける処理

        // 表示が0だけのときはその0を消してから入力文字を追加
        if ("0".contentEquals(expression)) {
            expression.setLength(0);
        }
        expression.append(ch); // 式が0の時は0を新しい入力で置き換え、それ以外の式の末尾に新しい文字を足していく

        state = InputState.INPUT_NUMBER;
        resultJustShown = false;
        clearedJustNow = false;
        return true;
    }

    // 小数点を押下した時の処理メソッド
    // @return 追加できた場合はtrue、できなかった場合はfalse
    public boolean appendDot() {
    	
    	// 電卓がエラーで小数点を押下した場合、小数点は受け付けない
        if (state == InputState.ERROR) {
            return false;
        }
        
        // 計算結果直後、小数点が押されたタイミングで一度全部クリアする
        if (resultJustShown) {
            clearAll();
            clearedJustNow = false;
        }

        // すでに小数点が含まれている場合は追加しない
        if (currentInput.indexOf(".") >= 0) {
            return false;
        }

        // 最大桁数を超える場合は追加しない
        if (countDigits(currentInput) > maxDigits) {
            return false;
        }

        // 演算子の直後に小数点が押された場合は0.から始める
        if (state == InputState.INPUT_OPERATOR && currentInput.length() == 0) { // 今の状態が演算子入力直後
            currentInput.append("0.");       // 演算子のあとに.を押したら、次の数は0.から始める
            expression.append("0.");         // 画面に出す式を同じ内容で同期
            state = InputState.INPUT_NUMBER; // 数字を入力している途中の状態
            resultJustShown = false;         // 今は計算結果を表示した直後ではない
            clearedJustNow = false;          // 今はクリアをした直後でもない
            return true;
        }

        // 入力が空なら0.を補って小数入力を開始
        if (currentInput.length() == 0) {             // まだ1文字も数字を押していない状態で小数点が押下された場合の処理
            if (expression.length() == 1 && expression.charAt(0) == '-') { // -記号1文字だけを出している状態
                currentInput.append("0.");            // 入力中の数値を0.から始める（-0. の「0.」部分）
                expression.append("0.");              // 式の末尾にも0.を足して、式を-0.にする
            } else {
                currentInput.append("0.");           // 入力中の数値として0.から始める
                if ("0".contentEquals(expression)) { // 起動直後やクリア直後で、画面に0だけ出ている状態を検出
                    expression.setLength(0);         // その0を消して式を空にする
                }
                expression.append("0.");             // 式の末尾に0.を追加
            }

            state = InputState.INPUT_NUMBER;        // 今は数字入力中の状態と記録
            resultJustShown = false;                // 直前に計算結果を表示した直後ではないとしておく
            clearedJustNow = false;                 // クリアした直後でもないとしておく
            return true;
        }

        // それ以外は通常どおり小数点を追加
        currentInput.append('.');        // 今入力している数値を、小数点付きの数にする
        expression.append('.');          // 画面上の式の文字列にも小数点を1文字追加して、入力中の数値と式表示を同期
        state = InputState.INPUT_NUMBER; // 今は数字入力中の状態と記録
        resultJustShown = false;         // 直前に計算結果を表示した直後ではないとしておく
        clearedJustNow = false;          // クリアした直後でもないとしておく
        return true;
    }

    // 演算子を設定
    // 実際の処理はinputOperator()に委譲
    // @param op 入力された演算子
    public void setOperator(Operator op) {
        inputOperator(op);
    }

    // 演算子を入力し、必要に応じて途中計算を行う
    // @param op入力された演算子
    public void inputOperator(Operator op) {
    	
    	// エラー状態では、-だけを負の数入力開始として許可する
    	if (state == InputState.ERROR) {  // 現在の電卓の状態がエラーかどうかを確認する
    	    if (op == Operator.SUB) {     // 押された演算子が-（引き算）かどうかを判定する
    	        clearAll();               // エラー状態を含めて電卓の内部状態をいったん初期化する
    	        clearedJustNow = false;   // クリアした直後、フラグはここでは特別扱いしないためfalseに戻す

    	        currentInput.setLength(0);       // 現在入力中の数値バッファを空にする
    	        currentInput.append("-");        // 現在入力中の値として、先頭のマイナス記号だけをセットする（負の数入力開始）
    	        expression.setLength(0);         // 画面表示用の式バッファも空にする
    	        expression.append("-");          // 式としても-だけを表示し、負の数を入力し始める状態にする
    	        state = InputState.INPUT_NUMBER; // 状態を数字入力中にして、これ以降の数字を負の数として受け付ける
    	        resultJustShown = false;         // 直前に結果を表示した直後の特別フラグをオフにしておく
    	        return;
    	    }
    	    return;
    	}
    	
    	// 演算子が未入力の場合は、何もしない
        if (op == null) {
            return;
        }

        // クリア直後は、マイナス以外の演算子入力を無効
        if (clearedJustNow && state == InputState.READY && op != Operator.SUB) {
            return;
        }

        if (resultJustShown) {                     //結果表示直後に演算子押下時の特別処理
            resultJustShown = false;               // 結果表示直後をリセット
            if (leftOperand != null) {             // 結果があり、かつその値を左辺として使える場合のみ連続計算に入る
                pendingOp = op;                    // 次に実行する予定の演算子を覚えておく
                expression.setLength(0);           // 画面に表示している式を空にする
                expression.append(FormatterUtil.formatResultForDisplay(leftOperand)); // 結果を次の計算の左辺として式にセットし直し
                appendOrReplaceOperator(op);       // 新しい演算子をくっつける
                currentInput.setLength(0);         // 右側の入力バッファを空にする
                state = InputState.INPUT_OPERATOR; // 演算子入力直後の状態
                clearedJustNow = false;            // 結果から連続計算に入っただけで、Cボタンを押下したわけではないので、クリア直後扱いにしない
                return;
            }
        }

        // 何も入力されていない状態で演算子が押された場合の処理
        if (state == InputState.READY && currentInput.length() == 0 && leftOperand == null) {
            if (op == Operator.SUB) {            // 先頭の-を演算子としてではなく、負の数の入力開始として扱う	
                currentInput.setLength(0);       // ここから新しい数字入力を始めるためリセット
                currentInput.append("-");        // 負の数の符号部分だけが入っている状態
                expression.setLength(0);         // 画面に表示している式をリセット
                expression.append("-");          // 画面には−だけが表示
                state = InputState.INPUT_NUMBER; // 数字入力中の状態
                resultJustShown = false;         // 今は計算結果を表示した直後ではない
                clearedJustNow = false;          // 今はクリアした直後でもない
            } else {
                startWithZero(op); // 何もない状態で+を押したら、0+(これから入力する数)の形で計算を始める
            }
            return;
        }

        // 演算子が連続入力された場合は、最後の演算子を置き換える
        if (state == InputState.INPUT_OPERATOR) { // 演算子入力直後かチェック
            appendOrReplaceOperator(op);          // 末尾に追加するか、すでにある演算子を置き換えるか、という処理をするメソッド
            pendingOp = op;                       // 演算子の予約し直し
            clearedJustNow = false;               // 直前にクリアした直後の状態ではない
            return;
        }
        
        // 数字がないのに演算子を押下しても、計算する材料がないのでスルーする
        if (currentInput.length() == 0) {
            return;
        }
        
        // マイナス記号だけが入力されている状態なら、何もせず終了
        if ("-".equals(currentInput.toString())) {
            return;
        }

        // 今入力中の数字を実際に計算に使える数値に変換する
        BigDecimal currentValue = safeParse(currentInput.toString());

        // 左辺が未設定なら現在値を左辺にする
        if (leftOperand == null) {
            leftOperand = currentValue;
        } else if (pendingOp != null) {
        	
            // 左辺と現在値で途中計算を行う
            BigDecimal applied = apply(leftOperand, currentValue, pendingOp);
            if (state == InputState.ERROR) {
                return;
            }
            leftOperand = applied;
        }

        pendingOp = op;                    // これから計算する演算子を保存
        currentInput.setLength(0);         // 右辺の数字入力をクリアして、新しい数字入力を始められるようにする
        expression.setLength(0);           // 直前の結果や式を一度クリアして、新しい式を作り直す
        expression.append(FormatterUtil.formatResultForDisplay(leftOperand)); // 直前の結果を画面表示用に整形して、式として追加する
        appendOrReplaceOperator(op);       // 今追加した結果の後ろに、今回押された演算子を追加するか置き換える

        state = InputState.INPUT_OPERATOR; // 状態を演算子入力直後に切り替え、次の数字入力は右辺として扱う
        resultJustShown = false;           // 結果を表示した直後の特別フラグをオフにする
        clearedJustNow = false;            // 通常の入力状態にする
    }

    // イコール操作を実行
    // evaluate()を呼び出して最終計算を行う
    public void equalsOp() {
        try {
            evaluate();
        } catch (Exception e) {         // 現在の式と状態を使って計算を行う（＝ボタンの本体処理）
            ErrorHandler.handle(e);     // 計算中に何か例外が発生した場合は、共通のエラーハンドラに処理を委ねる
            state = InputState.ERROR;   // 電卓全体の状態を「エラー」に切り替える
            currentInput.setLength(0);  // 入力中の数値をクリアして空にする
            expression.setLength(0);    // 式表示用のバッファもクリアして空にする
            expression.append("エラー"); // 画面にエラーという文字を表示する
        }
    }

    // 現在の左辺・右辺・演算子を使って最終計算を行う
    public void evaluate() {
    	// すでに電卓がエラー状態なら、これ以上計算せずに何もせず終了する
        if (state == InputState.ERROR) {
            return;
        }

        // 演算子が未設定、左辺が未設定、右辺の入力が空のいずれかなら計算できないので終了する
        if (pendingOp == null || leftOperand == null || currentInput.length() == 0) {
            return;
        }

        // 現在入力が「-」だけ（負の数の入力途中）なら、まだ有効な数値ではないので終了する
        if ("-".equals(currentInput.toString())) { 
            return;
        }

        BigDecimal right = safeParse(currentInput.toString());    // 現在入力中の文字列を安全に BigDecimal に変換して右辺の値とする
        BigDecimal result = apply(leftOperand, right, pendingOp); // 左辺・右辺・保留中の演算子を使って実際の計算を行い、結果を result に格納する

        // 計算中にエラーが発生して状態がエラーになっていたら、結果の表示は行わずに終了する
        if (state == InputState.ERROR) {
            return;
        }

        leftOperand = result;      // 正常に計算できたので、その結果を新しい左辺として保存する
        pendingOp = null;          // 今回の計算で使った演算子は消し、次に備えて保留中の演算子をリセットする
        currentInput.setLength(0); // 入力中の数値をクリアして空にする
        expression.setLength(0);   // 式表示もクリアして、古い式を全部消す
        expression.append(FormatterUtil.formatResultForDisplay(result)); // 計算結果を画面表示用に整形して、式として表示する
        state = InputState.READY;  // 計算が完了したので状態を次の入力待ちに戻す
        resultJustShown = true;    // 今は直前に結果を表示したばかりというフラグをオンにする
        clearedJustNow = false;    // クリア直後ではないので、そのフラグはオフにしておく
    }

    // 2つの値に対して演算子に応じた計算を行う
    // @param left 左辺
    // @param right 右辺
    // @param op 演算子
    // @return 計算結果
    private BigDecimal apply(BigDecimal left, BigDecimal right, Operator op) {
        try {
        	// 演算子の種類に応じて処理を切り替える
            switch (op) {
                case ADD: // 足し算：left + right を計算して返す
                    return left.add(right);
                case SUB: // 引き算：left - right を計算して返す
                    return left.subtract(right);
                case MUL: // 掛け算：left × right を計算して返す
                    return left.multiply(right);
                case DIV: // 割り算：0で割ろうとしていないかチェックする
                    if (right.compareTo(BigDecimal.ZERO) == 0) { // 右辺が0なら、0で割ることはできないので計算用の例外を投げる
                        throw new ArithmeticException("0で割ることはできません");
                    }
                    // left ÷ right を 16桁で四捨五入して計算し、末尾の不要な0を取り除いて返す
                    return left.divide(right, 16, RoundingMode.HALF_UP).stripTrailingZeros();
                default: // 想定外の演算子の場合は、右辺の値をそのまま返す
                    return right;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);     // 計算中に何か例外が発生したら、共通のエラーハンドラでログなどの処理を行う
            state = InputState.ERROR;   // 電卓全体の状態をエラーに切り替える
            leftOperand = null;         // 左辺の値をクリアして、計算途中の情報を消す
            pendingOp = null;           // 保留中の演算子もクリアして、次の演算予約をリセットする
            currentInput.setLength(0);  // 現在入力中の数値をクリアして空にする
            expression.setLength(0);    // 式表示もクリアして、古い式を全部消す
            expression.append("エラー"); // 画面にエラーと表示
            resultJustShown = false;    // 結果表示直後フラグはオフにしておく（結果は正しく出ていないため）
            clearedJustNow = false;     // 「クリア直後」フラグもオフにしておく（今回の状態はエラーによるリセット）
            return BigDecimal.ZERO;     // 例外時の戻り値として 0 を返す（呼び出し側からも扱いやすい安全な値）
        }
    }

    // 文字列をBigDecimalに変換
    // 不完全な入力の場合は0を返す
    // @param s数値文字列
    // @return変換後のBigDecimal
    private BigDecimal safeParse(String s) {
    	// 数字として成立していない入力は0を返す
        if (s == null || s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(s);   // それ以外の正常な文字列は BigDecimal に変換して返す
    }

    // 指定された文字列中の数字の個数を数える
    // @param text数える対象
    // @return数字の個数
    private int countDigits(CharSequence text) {
        int count = 0; // 先頭から末尾まで1文字ずつ走査する
        for (int i = 0; i < text.length(); i++) {
        	
        	// この位置の文字が数字かどうか判定する（'0'〜'9'だけを桁として数える）
            if (Character.isDigit(text.charAt(i))) {
                count++;
            }
        }
        return count; // 見つけた数字の個数を返す
    }

    // 現在入力中の値に合わせてexpressionを更新
    private void syncExpressionWithCurrentInput() {
        int idx = findLastOperatorIndex(); // 式の中で最後に出てくる演算子の位置を調べる（演算子があるかどうかの目安）
        expression.setLength(0);           // 式表示をクリアして再構築する

        // 左辺と演算子が揃っている場合は「左辺 + 演算子 + 現在入力中の数値」という形で式を作る
        if (leftOperand != null && pendingOp != null) {   // 左辺の値を画面表示用に整形して式に追加する
            expression.append(FormatterUtil.formatResultForDisplay(leftOperand)); // 保留中の演算子を記号に変換して式に追加する（例：+ や -）
            expression.append(operatorSymbol(pendingOp)); // 右側の現在入力中の数値をそのまま式に追加する
            expression.append(currentInput);              // このケースではここで同期完了なのでメソッドを終了する
            return;
        }

        // 左辺や演算子がない場合でも、演算子の位置が見つかっているときはcurrentInputだけを式として表示する
        if (idx >= 0) {                      // 現在入力中の数値を式にそのまま反映する
            expression.append(currentInput); // 同期完了なので終了する
            return;
        }
        
        // 演算子も見つからない場合は、単純に currentInput の内容だけを式として表示する
        expression.append(currentInput);
    }

    // 式の末尾に演算子を追加するか、すでにある演算子を置き換える
    // @param op追加または置換する演算子
    private void appendOrReplaceOperator(Operator op) {
        char symbol = operatorSymbol(op); // 渡された演算子オブジェクトから、画面に表示する記号（+, -, ×, ÷など）を取得する

        // 式がまだ空の場合は、そのまま演算子記号だけを追加して終わる
        if (expression.length() == 0) {
            expression.append(symbol);
            return;
        }

        int last = expression.length() - 1; // 式の最後の位置（末尾インデックス）を取得する
        char end = expression.charAt(last); // 式の末尾にある1文字を取り出す（最後に入力された記号や数字）

        // 末尾の文字が演算子記号（+, -, ×, ÷ など）であれば、その演算子を今回の演算子に置き換える
        if (isOperatorChar(end)) {
            expression.setCharAt(last, symbol); // 末尾が数字など演算子でない場合は、末尾に新しい演算子記号を追加する
            return;
        }

        expression.append(symbol);
    }

    // 式の中で最後に現れる演算子の位置を返す
    // @return 最後の演算子の位置。見つからない場合は-1
    private int findLastOperatorIndex() {
    	// 式の末尾から先頭方向に1文字ずつ逆順で走査する
        for (int i = expression.length() - 1; i >= 0; i--) {
            char c = expression.charAt(i); // 現在位置の文字を取り出す
            // この文字が演算子記号（+, -, ×, ÷ など）なら、その位置を演算子のインデックスとして返す
            if (isOperatorChar(c)) {
                return i;
            }
        }
        return -1; // どこにも演算子が見つからなければ -1 を返す（演算子なしという意味）
    }

    // 指定文字が演算子かどうかを判定
    // @param c判定対象の文字
    // @return演算子ならtrue
    private boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '×' || c == '÷';
    }

    // 演算子を画面表示用の記号に変換
    // @param op変換対象の演算子
    // @return表示用記号
    private char operatorSymbol(Operator op) {
        switch (op) {
            case ADD:
                return '+';
            case SUB:
                return '-';
            case MUL:
                return '×';
            case DIV:
                return '÷';
            default:
                return '?';
        }
    }
}