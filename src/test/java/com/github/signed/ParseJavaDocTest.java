package com.github.signed;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite.Description;
import org.openrewrite.NlsRewrite.DisplayName;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.JavadocVisitor;
import org.openrewrite.java.tree.Javadoc;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.util.stream.Collectors;

import static org.openrewrite.java.Assertions.java;

public class ParseJavaDocTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {

        spec.recipe(new JavadocReproducer());
    }

    /**
     * When looking at the Javadoc.DocComment.body() I'm surprised
     * to see three elements for the second line
     * <ol>
     *     <li>Javadoc.Text(text=" ")
     *     <li>Javadoc.Text(text="2nd line")</li>
     *     <li>LineBreak=()</li></li>
     * </ol>
     * I expected only two (like for 3rd line)
     * <ol>
     *     <li>Javadoc.Text(text=" 2nd line")</li>
     *     <li>LineBreak=()</li>
     * </ol>
     */
    @Test
    void firstLineIsParsedInTwoSeparateText() {
        rewriteRun(
          java(
            """              
              /**
               * 2nd line
               * 3rd line
               */
              public class Test {
              
              }
              """));
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    private static class JavadocReproducer extends Recipe {
        @Override
        public @DisplayName String getDisplayName() {
            return "Javadoc reproducer";
        }

        @Override
        public @Description String getDescription() {
            return "Why split the line into two separate Text.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new JavaVisitor<>() {
                @Override
                protected JavadocVisitor<ExecutionContext> getJavadocVisitor() {
                    return new JavadocVisitor<>(this) {
                        @Override
                        public Javadoc visitDocComment(Javadoc.DocComment javadoc, ExecutionContext executionContext) {
                            var dc = (Javadoc.DocComment) super.visitDocComment(javadoc, executionContext);
                            System.out.println(dc.getBody().stream().map(it -> {
                                if (it instanceof Javadoc.Text) {
                                    return "Javadoc.Text(text=\"" + ((Javadoc.Text) it).getText() + "\")";
                                } else if (it instanceof Javadoc.LineBreak) {
                                    return "LineBreak=()";
                                }
                                return it.toString();
                            }).collect(Collectors.joining("\n")));
                            return dc;
                        }
                    };
                }
            };
        }
    }
}
