package com.nabd.ai.local.intelligence.parser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KotlinSymbolExtractorTest {

    @Test
    fun `extracts class and function symbols correctly`() {
        val extractor = KotlinSymbolExtractor()
        val code = """
            package test
            
            import java.util.List
            
            class MyClass {
                val myProp = 1
                
                fun myFunction() {
                }
            }
            
            object MyObject
        """.trimIndent()
        
        val symbols = extractor.parse(code, "test.kt")
        val imports = extractor.extractImports(code)
        
        assertEquals(1, imports.size)
        assertEquals("java.util.List", imports[0])
        
        val myClass = symbols.find { it.name == "MyClass" }
        assertNotNull(myClass)
        assertEquals(SymbolType.CLASS, myClass?.type)
        
        val myProp = symbols.find { it.name == "myProp" }
        assertNotNull(myProp)
        assertEquals(SymbolType.PROPERTY, myProp?.type)
        
        val myFun = symbols.find { it.name == "myFunction" }
        assertNotNull(myFun)
        assertEquals(SymbolType.FUNCTION, myFun?.type)
        
        val myObj = symbols.find { it.name == "MyObject" }
        assertNotNull(myObj)
        assertEquals(SymbolType.OBJECT, myObj?.type)
    }
}
