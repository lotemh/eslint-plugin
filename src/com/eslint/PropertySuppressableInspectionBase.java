package com.eslint;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInspection.CustomSuppressableInspectionTool;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class PropertySuppressableInspectionBase extends LocalInspectionTool implements CustomSuppressableInspectionTool {
    private static final Logger LOG = Logger.getInstance("#com.intellij.lang.properties.PropertySuppressableInspectionBase");

    @NotNull
    public String getGroupDisplayName() {
        return ESLintBundle.message("eslint.inspection.group.name");
    }

    public SuppressIntentionAction[] getSuppressActions(final PsiElement element) {
        PsiNamedElement pe = getProblemElement(element);

        return new SuppressIntentionAction[]{new SuppressForStatement(getShortName()), new SuppressForFile(getShortName())};
    }

    public boolean isSuppressedFor(@NotNull PsiElement element) {
//        Property property = PsiTreeUtil.getParentOfType(element, Property.class, false);
//        JSFileImpl file;
//        if (property == null) {
//            PsiFile containingFile = element.getContainingFile();
//            if (containingFile instanceof JSFileImpl) {
//                file = (JSFileImpl) containingFile;
//            } else {
//                return false;
//            }
//        } else {
//            PsiElement prev = property.getPrevSibling();
//            while (prev instanceof PsiWhiteSpace || prev instanceof PsiComment) {
//                if (prev instanceof PsiComment) {
//                    @NonNls String text = prev.getText();
//                    if (text.contains("suppress") && text.contains('"' + getShortName() + '"')) return true;
//                }
//                prev = prev.getPrevSibling();
//            }
//            file = property.getPropertiesFile();
//        }
//        PsiElement leaf = file.getContainingFile().findElementAt(0);
//        while (leaf instanceof PsiWhiteSpace) leaf = leaf.getNextSibling();
//
//        while (leaf instanceof PsiComment) {
//            @NonNls String text = leaf.getText();
//            if (text.contains("suppress") && text.contains('"' + getShortName() + '"') && text.contains("file")) {
//                return true;
//            }
//            leaf = leaf.getNextSibling();
//            if (leaf instanceof PsiWhiteSpace) leaf = leaf.getNextSibling();
//            // comment before first property get bound to the file, not property
//            if (leaf instanceof PropertiesList && leaf.getFirstChild() == property && text.contains("suppress") && text.contains("\"" + getShortName() + "\"")) {
//                return true;
//            }
//        }

        return false;
    }

    private static class SuppressForStatement extends SuppressIntentionAction {
        private final String rule;

        public SuppressForStatement(String rule) {
            this.rule = rule;
        }

        @NotNull
        public String getText() {
            return ESLintBundle.message("unused.property.suppress.for.statement");
        }

        @NotNull
        public String getFamilyName() {
            return ESLintBundle.message("unused.property.suppress.for.statement");
        }

        public boolean isAvailable(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) {
            final JSElement property = PsiTreeUtil.getParentOfType(element, JSElement.class);
            return property != null && property.isValid();
        }

        public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) throws IncorrectOperationException {
            final PsiFile file = element.getContainingFile();
            if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;

//            InspectionManager inspectionManager = InspectionManager.getInstance(project);
//            ProblemDescriptor descriptor = inspectionManager.createProblemDescriptor(element, element, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false);

            final JSElement property = PsiTreeUtil.getParentOfType(element, JSElement.class);
            LOG.assertTrue(property != null);
            final int start = property.getTextRange().getStartOffset();

            @NonNls final Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
            LOG.assertTrue(doc != null);
            final int line = doc.getLineNumber(start);
            final int lineStart = doc.getLineStartOffset(line);

            doc.insertString(lineStart, "// eslint suppress inspection \"" + rule + "\"\n");
        }
    }

    private static class SuppressForFile extends SuppressIntentionAction {
        private final String rule;

        public SuppressForFile(String rule) {
            this.rule = rule;
        }

        @NotNull
        public String getText() {
            return ESLintBundle.message("unused.property.suppress.for.file");
        }

        @NotNull
        public String getFamilyName() {
            return ESLintBundle.message("unused.property.suppress.for.file");
        }

        public boolean isAvailable(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) {
            return element.isValid() && element.getContainingFile() instanceof JSFile;
        }

        public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) throws IncorrectOperationException {
            final PsiFile file = element.getContainingFile();
            if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;

            @NonNls final Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
            LOG.assertTrue(doc != null, file);

//            doc.insertString(0, "// eslint suppress inspection \"" + rule + "\" for whole file\n");
            doc.insertString(0, "/* eslint-disable */\n");
        }
    }
}