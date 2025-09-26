package com.spreadsheet.integration

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.PolyglotException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PythonIntegrationTest {

    @Test
    fun `should execute simple python script and return result`() {
        // 1. GraalVMコンテキストを初期化
        val context = Context.newBuilder("python").allowHostAccess(HostAccess.ALL).build()

        // 2. 実行するPythonスクリプトを定義
        val pythonScript = """
        def add(a, b):
            return a + b
        
        add(10, 20)
        """.trimIndent()

        // 3. Pythonスクリプトを実行し、結果を取得
        val result = context.eval("python", pythonScript)

        // 4. 結果を検証
        assertEquals(30, result.asInt())
    }

    @Test
    fun `can pass and receive a list`() {
        val context = Context.newBuilder("python").allowHostAccess(HostAccess.ALL).build()
        val pythonScript = """
        def process_list(data):
            return [x * 2 for x in data]
        """.trimIndent()

        context.eval("python", pythonScript)
        
        val kotlinList = listOf(1, 2, 3)
        val processListFunc = context.getBindings("python").getMember("process_list")
        val result = processListFunc.execute(kotlinList)

        assertEquals(3, result.arraySize)
        assertEquals(2, result.getArrayElement(0).asInt())
        assertEquals(4, result.getArrayElement(1).asInt())
        assertEquals(6, result.getArrayElement(2).asInt())
    }

    @Test
    fun `can receive a dictionary created in python`() {
        val context = Context.newBuilder("python").allowHostAccess(HostAccess.ALL).build()
        val pythonScript = """
        def create_dict():
            return {'a': 1, 'b': 'hello', 'c': 3.14}
        """.trimIndent()

        context.eval("python", pythonScript)

        val createDictFunc = context.getBindings("python").getMember("create_dict")
        val result = createDictFunc.execute()

        // Use hash-related methods for dictionary access
        assertEquals(true, result.hasHashEntries())
        
        // Access values using getHashValue
        val valueA = result.getHashValue("a")
        val valueB = result.getHashValue("b")
        val valueC = result.getHashValue("c")

        assertEquals(1, valueA.asInt())
        assertEquals("hello", valueB.asString())
        assertEquals(3.14, valueC.asDouble(), 0.001)
    }

    @Test
    fun `can call python function with arguments`() {
        val context = Context.newBuilder("python").allowHostAccess(HostAccess.ALL).build()
        val pythonScript = """
        def subtract(a, b):
            return a - b
        """.trimIndent()
        context.eval("python", pythonScript)

        val subtractFunc = context.getBindings("python").getMember("subtract")
        val result = subtractFunc.execute(20, 5)

        assertEquals(15, result.asInt())
    }

    @Test
    fun `can receive a simple string`() {
        val context = Context.newBuilder("python").allowHostAccess(HostAccess.ALL).build()
        val result = context.eval("python", "'hello'")
        assertEquals("hello", result.asString())
    }

    @Test
    fun `should throw exception for python syntax error`() {
        val context = Context.newBuilder("python").allowHostAccess(HostAccess.ALL).build()
        val invalidSyntaxScript = "def func(a, b):\n  return a + b +"

        val exception = assertThrows<PolyglotException> {
            context.eval("python", invalidSyntaxScript)
        }
        // Check that the exception is about a syntax error
        assert(exception.message?.contains("SyntaxError") == true)
    }

    @Test
    fun `should throw exception for python runtime error`() {
        val context = Context.newBuilder("python").allowHostAccess(HostAccess.ALL).build()
        val runtimeErrorScript = "1 / 0"

        val exception = assertThrows<PolyglotException> {
            context.eval("python", runtimeErrorScript)
        }
        // Check that the exception is about a division by zero
        assert(exception.message?.contains("ZeroDivisionError") == true)
    }
}
