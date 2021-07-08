package com.example.myproc

import com.example.mylibrary.GeneratePresentationTitle
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

@AutoService(Processor::class) // Registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_11) // to support Java 8
class MyProcessor : AbstractProcessor() {

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        val generatedDir = processingEnv.options["kapt.kotlin.generated"] ?: return false

        roundEnv
            ?.getElementsAnnotatedWith(GeneratePresentationTitle::class.java)
            ?.mapNotNull { generateFile(it, generatedDir) }

        return true
    }

    private fun generateFile(element: Element, directory: String) {
        val packageName = processingEnv.elementUtils.getPackageOf(element).toString()
        val fileName = "Netguru${element.simpleName}"

        val classBuilder = TypeSpec.classBuilder(fileName)
        classBuilder.addFunction(
            FunSpec.builder("generatePresentationTitle")
                .addStatement("return \"Your own annotation processor.\"")
                .returns(String::class)
                .build()
        )

        val fileBuilder = FileSpec.builder(packageName, fileName)
        val file = File(directory)
        fileBuilder
            .addType(classBuilder.build()).build()
            .writeTo(file)
    }

    // Declaring supported annotations
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
        GeneratePresentationTitle::class.java.name
    )

    // Supported Java versions
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

}