package com.github.signed;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class JavaDocTransformTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {

        spec.recipe(new JavaDocTransform("^Created by .*$"));
    }

    @DocumentExample
    @Test
    void removeMatchingText() {
        rewriteRun(
          java(
            """              
              /**
               * Keep me
               * Created by Alice
               * Created by Bob
               * Keep it
               */
              public class Test {
              
              }
              """,
            """              
              /**
               * Keep me
               * Keep it
               */
              public class Test {
              
              }
              """));
    }

    @DocumentExample
    @Test
    void removeEntireCommentInCaseNothingRemains() {
        rewriteRun(
          java(
            """              
              /**
               * Created by Name
               */
              public class Test {
              
              }
              """,
            """              
              public class Test {
              
              }
              """));
    }

}