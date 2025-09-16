package com.github.signed;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite.Description;
import org.openrewrite.NlsRewrite.DisplayName;
import org.openrewrite.Option;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.JavadocVisitor;
import org.openrewrite.java.tree.Javadoc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Value
@EqualsAndHashCode(callSuper = false)
public class JavaDocTransform extends Recipe {
    @Override
    public @DisplayName String getDisplayName() {
        return "JavaDoc transform";
    }

    @Override
    public @Description String getDescription() {
        return "Drop drop JavaDoc lines matching the regular expression.";
    }

    @Option(displayName = "Find",
            description = "The regext to find the text to delete.",
            example = "^Created by .*$")
    String find;


    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            @Override
            protected JavadocVisitor<ExecutionContext> getJavadocVisitor() {
                return new JavadocVisitor<ExecutionContext>(this) {

                    @Override
                    public Javadoc visitDocComment(Javadoc.DocComment docComment, ExecutionContext ctx) {
                        Javadoc.DocComment dc = (Javadoc.DocComment) super.visitDocComment(docComment, ctx);
                        List<Javadoc> newBody = new ArrayList<>();
                        boolean isChanged = false;
                        boolean removeNextLineBreak = false;

                        for (Javadoc javadoc : dc.getBody()) {

                            if (removeNextLineBreak) {
                                if (javadoc instanceof Javadoc.LineBreak) {
                                    removeNextLineBreak = false;
                                }
                            } else if (javadoc instanceof Javadoc.Text) {
                                String text = ((Javadoc.Text) javadoc).getText();
                                if (!Pattern.compile(find).matcher(text).matches()) {
                                    newBody.add(javadoc);
                                } else {
                                    isChanged = true;
                                    removeNextLineBreak = true;
                                }
                            } else {
                                newBody.add(javadoc);
                            }

                        }
                        if (isChanged) {
                            if (isBlank(getCursor(), newBody)) {
                                return null;
                            }
                            dc = dc.withBody(newBody);
                        }
                        return dc;
                    }
                };
            }
        };
    }

    static boolean isBlank(Cursor cursor, List<Javadoc> newBody) {
        return newBody.stream().allMatch(jd -> {
            PrintOutputCapture<Object> p = new PrintOutputCapture<>(null);
            jd.printer(cursor).visit(jd, p);
            String currentLine = p.getOut().trim();
            return StringUtils.isBlank(currentLine) || "*".equals(currentLine);
        });
    }
}
