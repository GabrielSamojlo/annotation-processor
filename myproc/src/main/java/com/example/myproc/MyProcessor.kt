package com.example.myproc

import com.example.mylibrary.AutoDto
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

@AutoService(Processor::class) // Registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
class MyProcessor : AbstractProcessor() {

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        val generatedDir = processingEnv.options["kapt.kotlin.generated"] ?: return false

        roundEnv
            ?.getElementsAnnotatedWith(AutoDto::class.java)
            ?.mapNotNull { generateFile(it, generatedDir) }

        return true
    }

    private fun generateFile(element: Element, directory: String) {
        val classFields = element.enclosedElements.filter { it.kind == ElementKind.FIELD }
        val packageName = processingEnv.elementUtils.getPackageOf(element).toString()
        val fileName = "${element.simpleName}Dto"

        val classBuilder = TypeSpec.classBuilder(fileName)
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class.java)
            .primaryConstructor(
                buildConstructor(classFields)
            )
            .addProperties(
                buildProperties(classFields)
            )

        FileSpec.builder(packageName, fileName)
            .addType(classBuilder.build()).build()
            .writeTo(
                File(directory)
            )
    }

    private fun buildConstructor(fields: List<Element>): FunSpec {
        val builder = FunSpec.constructorBuilder()

        fields.forEach {
            builder.addParameter(name = it.simpleName.toString(), type = it.asType().asTypeName())
        }

        return builder.build()
    }

    private fun buildProperties(fields: List<Element>): List<PropertySpec> {
        return fields.map {
            PropertySpec
                .builder(name = it.simpleName.toString(), type = it.asType().asTypeName())
                .initializer(it.simpleName.toString())
                .addAnnotation(
                    AnnotationSpec
                        .builder(type = SerialName::class)
                        .addMember(format = "%L = %S", "value", it.simpleName.toString())
                        .build()
                ).build()
        }
    }

    // Declaring supported annotations
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
        AutoDto::class.java.name
    )

    // Supported Java versions
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

}
