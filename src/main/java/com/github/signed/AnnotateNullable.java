package com.github.signed;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.ShortenFullyQualifiedTypeReferences;
import org.openrewrite.java.search.FindAnnotations;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;

import static org.openrewrite.java.tree.J.MethodDeclaration;

@Value
@EqualsAndHashCode(callSuper = false)
public class AnnotateNullable extends Recipe {
    @Override
    public String getDisplayName() {
        return "Stand in";
    }

    @Override
    public String getDescription() {
        return "Stand in.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new SayHelloVisitor();
    }

    public class SayHelloVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public MethodDeclaration visitMethodDeclaration(MethodDeclaration methodDeclaration, ExecutionContext executionContext) {
            if (!FindAnnotations.find(methodDeclaration, "@org.jspecify.annotations.Nullable").isEmpty()) {
                return methodDeclaration;
            }
            MethodDeclaration md = super.visitMethodDeclaration(methodDeclaration, executionContext);

            J.MethodDeclaration annotatedMethod = JavaTemplate.builder("@org.jspecify.annotations.Nullable")
                    .javaParser(JavaParser.fromJavaVersion().dependsOn(
                            String.format("package %s;public @interface %s {}", "org.jspecify.annotations", "Nullable")))
                    .build()
                    .apply(getCursor(), md.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
            doAfterVisit(ShortenFullyQualifiedTypeReferences.modifyOnly(annotatedMethod));
            return annotatedMethod;

        }
    }
}
