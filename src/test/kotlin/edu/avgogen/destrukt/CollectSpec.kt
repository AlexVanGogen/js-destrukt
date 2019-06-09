package edu.avgogen.destrukt

import edu.avgogen.destrukt.cli.main
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

class CollectSpec : StringSpec({

    fun doTest(fileName: String) {
        val file = File("tests/$fileName.out.js")
        val expectedContents = file.readText()
        file.writeText("")
        main(arrayOf("tests/$fileName.js"))
        val actualContents = file.readText()
        try {
            actualContents shouldBe expectedContents
        } finally {
            file.writeText(expectedContents)
        }
    }

    "assignments" {
        doTest("assigns")
    }

    "var declarations" {
        doTest("vars")
    }

    "const declarations" {
        doTest("consts")
    }

    "let declarations" {
        doTest("lets")
    }

    "mixed kinds of declarations" {
        doTest("differentTypes")
    }

    "non-sequential indices" {
        doTest("indices")
    }

    "duplicated declarations" {
        doTest("duplicatesInDeclarations")
    }

    "repeating indices in declarations" {
        doTest("repeatingIndices")
    }


    "long resulting arrays representing destructuring should be prohibited" {
        doTest("longDestructuring")
    }

    "variables that could not participate in destructuring must not be deleted" {
        doTest("nonConvertibleVarInDeclaration")
    }
})