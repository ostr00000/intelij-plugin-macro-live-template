package com.template;

import com.intellij.codeInsight.template.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class PropertyFinderMacro extends Macro {
    private final static String filename = "Makefile";
    private final static String property = "PLUGINNAME =";
    private final static int maxLine = 100;

    @Override
    public String getName() {
        return "pluginName";
    }

    @Override
    public String getPresentableName() {
        return this.getName() + "()";
    }

    @Nullable
    @Override
    public Result calculateResult(@NotNull Expression[] params, ExpressionContext context) {
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(context.getProject());

        return Optional
                .ofNullable(context.getEditor())
                .map(Editor::getDocument)
                .map(documentManager::getPsiFile)
                .map(PsiFile::getContainingDirectory)
                .flatMap(this::getPluginName)
                .orElse(null);

    }

    private Optional<Result> getPluginName(PsiDirectory directory) {

//      java 8
        Result a = Stream.of(directory)
                .map(PsiDirectory::getFiles)
                .flatMap(Arrays::stream)
                .map(this::getPluginName)
                .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
                .findFirst()
                .orElse(Optional.of(directory)
                        .map(PsiDirectory::getParentDirectory)
                        .flatMap(this::getPluginName)
                        .orElse(null)
                );

        return Optional.ofNullable(a);


//        java 9
//        return Stream.of(directory)
//                .map(PsiDirectory::getFiles)
//                .flatMap(Arrays::stream)
//                .map(this::getPluginName)
//                .flatMap(Optional::stream)
//                .findFirst()
//                .or(() -> Optional.of(directory)
//                        .map(PsiDirectory::getParentDirectory)
//                        .flatMap(this::getPluginName)
//                );
    }

    private Optional<Result> getPluginName(PsiFile file) {

        return Optional.of(file)
                .filter(this::hasCorrectFileName)
                .map(PsiFile::getViewProvider)
                .map(FileViewProvider::getDocument)
                .flatMap(this::getPluginName);
    }

    private Boolean hasCorrectFileName(PsiFile file) {
        return file.getName().equals(PropertyFinderMacro.filename);
    }

    private Optional<Result> getPluginName(Document document) {
        CharSequence data = document.getImmutableCharSequence();

        return IntStream.range(0, Integer.min(PropertyFinderMacro.maxLine, document.getLineCount()))
                .map(document::getLineEndOffset)
                .sequential()
                .collect(() -> new ArrayList<>(Collections.singleton(new Pair<>(0, -1))),
                        (acc, elem) -> acc.add(new Pair<Integer, Integer>(acc.get(acc.size() - 1).second + 1, elem)),
                        null)
                .stream()
                .filter(pair -> pair.second > 0)
                .map(pair -> data.subSequence(pair.first, pair.second))
                .map(CharSequence::toString)
                .filter(line -> line.startsWith(PropertyFinderMacro.property))
                .findFirst()
                .map(line -> line.substring(PropertyFinderMacro.property.length()))
                .map(name -> name.startsWith(" ") ? name.substring(1) : name)
                .map(TextResult::new);
    }
}