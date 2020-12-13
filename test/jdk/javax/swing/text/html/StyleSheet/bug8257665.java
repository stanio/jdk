/*
 * <Copyright>
 */

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.GlyphView;
import javax.swing.text.View;

/*
 * @test
 * @key headless
 * @bug 8257665
 * @summary  Tests inherited font-size with parent percentage specification.
 * @run main bug8257665
 */
public class bug8257665 {

    private static String text =
            "<html><head><style>" +
            "body { font-size: 14 }" +
            "div span { font-size: 150% }" +
            "span { font-size: 200% }" +
            "h2, .h2 { font-size: 150% }" +
            "</style></head><body>" +

            "<h2>Foo</h2>" +
            "<div class=h2>Bar</div>" +
            "<ol class=h2><li>Baz</li></ol>" +
            "<table class=h2><tr><td>Qux</td></tr></table>" +
            "<table><thead class=h2><tr><th>Qux</th></tr></thead></table>" +
            "<table><tr class=h2><td>Qux</td></tr></table>" +
            "<table><tr><td class=h2>Qux</td></tr></table>" +
            "<div><span>Quux</span></div>" +

            "</body></html>";

    private static int expectedFontSize = 21;
    private static int expectedAssertions = 8;

    private JEditorPane editor;

    public void setUp() {
        editor = new JEditorPane();
        editor.setContentType("text/html");
        editor.setText(text);

        View rootView = editor.getUI().getRootView(editor);
        rootView.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE); // layout
    }

    public void run() {
        int count = forEachTextRun(editor.getUI()
                .getRootView(editor), this::assertFontSize);
        if (count != expectedAssertions) {
            throw new AssertionError("assertion count expected ["
                    + expectedAssertions + "] but found [" + count + "]");
        }
    }

    private int forEachTextRun(View view, Consumer<GlyphView> action) {
        int tested = 0;
        for (int i = 0; i < view.getViewCount(); i++) {
            View child = view.getView(i);
            if (child instanceof GlyphView) {
                if (child.getElement()
                        .getAttributes().getAttribute("CR") == Boolean.TRUE) {
                    continue;
                }
                action.accept((GlyphView) child);
                tested += 1;
            } else {
                tested += forEachTextRun(child, action);
            }
        }
        return tested;
    }

    private void assertFontSize(GlyphView child) {
        printSource(child);
        int actualFontSize = child.getFont().getSize();
        if (actualFontSize != expectedFontSize) {
            throw new AssertionError("font size expected ["
                    + expectedFontSize + "] but found [" + actualFontSize +"]");
        }
    }

    private void printSource(View textRun) {
        try {
            editor.getEditorKit().write(System.out,
                    editor.getDocument(), textRun.getStartOffset(),
                    textRun.getEndOffset() - textRun.getStartOffset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Throwable {
        bug8257665 test = new bug8257665();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                test.setUp();
                test.run();
            } catch (Throwable e) {
                failure.set(e);
            }
        });
        if (failure.get() != null) {
            throw failure.get();
        }
    }

}
