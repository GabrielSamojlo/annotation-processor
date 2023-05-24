package com.example.myproc

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(DelicateKotlinPoetApi::class)
class MyProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(
            "com.example.mylibrary.AutoDto"
        )

        symbols
            .filter { it.validate() }
            .filterIsInstance<KSClassDeclaration>()
            .forEach { generateFile(it, codeGenerator) }

        return symbols.filter { !it.validate() }.toList()
    }

    private fun generateFile(classDeclaration: KSClassDeclaration, codeGenerator: CodeGenerator) {
        val fileName = "${classDeclaration.simpleName.asString()}Dto"
        val packageName = classDeclaration.packageName.asString()

        val classBuilder = TypeSpec.classBuilder(fileName)
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class.java)
            .primaryConstructor(
                buildConstructor(classDeclaration)
            )
            .addProperties(
                buildProperties(classDeclaration)
            )

        FileSpec.builder(packageName, fileName)
            .addType(classBuilder.build()).build()
            .writeTo(
                codeGenerator,
                Dependencies(true, classDeclaration.containingFile!!)
            )
    }

    private fun buildConstructor(classDeclaration: KSClassDeclaration): FunSpec {
        val builder = FunSpec.constructorBuilder()

        classDeclaration.primaryConstructor?.parameters?.forEach {
            builder.addParameter(name = it.name?.asString().orEmpty(), type = it.type.toTypeName())
        }

        return builder.build()
    }

    private fun buildProperties(classDeclaration: KSClassDeclaration): List<PropertySpec> {
        return classDeclaration.getAllProperties().map {
            PropertySpec
                .builder(name = it.simpleName.asString(), type = it.type.toTypeName())
                .initializer(it.simpleName.asString())
                .addAnnotation(
                    AnnotationSpec
                        .builder(type = SerialName::class)
                        .addMember(format = "%L = %S", "value", it.simpleName.asString())
                        .build()
                ).build()
        }.toList()
    }
}
