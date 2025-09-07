package com.github.signed;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class AnnotateNullableTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {

        spec.recipe(new AnnotateNullable());
    }

    @DocumentExample
    @Test
    void addNullableIfMethodReturnsNull() {
        rewriteRun(
          java(
            """              
                public class MyClass {
                    public String method() {
                        return null;
                    }
                }
              """,
            """              
              import org.jspecify.annotations.Nullable;
              
              public class MyClass {
                  @Nullable
                  public String method() {
                      return null;
                  }
              }
              """));
    }

    @Test
    void doNotAlterMethodIfNullableIsAlreadyPresent() {
        rewriteRun(java("""              
          import org.jspecify.annotations.Nullable;
          
          public class MyClass {
              public @Nullable String method() {
                  return null;
              }
          }
          """));
    }
}