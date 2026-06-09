package com.nabd.ai.local.agent.parser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ToolCallParserTest {

    private val parser = ToolCallParser()

    @Test
    fun `parse valid JSON tool call`() {
        val input = """
            {
              "tool": "calculator",
              "parameters": {
                "expression": "2+2"
              }
            }
        """.trimIndent()

        val result = parser.parse(input)
        assertTrue(result is ToolCallResult.Success)
        val success = result as ToolCallResult.Success
        assertEquals("calculator", success.toolCall.tool)
        assertEquals("2+2", success.toolCall.parameters["expression"])
    }

    @Test
    fun `parse JSON wrapped in markdown`() {
        val input = """
            ```json
            {
              "tool": "time",
              "parameters": {}
            }
            ```
        """.trimIndent()

        val result = parser.parse(input)
        assertTrue(result is ToolCallResult.Success)
        val success = result as ToolCallResult.Success
        assertEquals("time", success.toolCall.tool)
    }

    @Test
    fun `parse invalid JSON returns error`() {
        val input = """
            {
              "tool": "broken",
              "parameters": 
        """.trimIndent()

        val result = parser.parse(input)
        assertTrue(result is ToolCallResult.Error)
    }

    @Test
    fun `parse missing tool field returns error`() {
        val input = """
            {
              "parameters": {}
            }
        """.trimIndent()

        val result = parser.parse(input)
        assertTrue(result is ToolCallResult.Error)
        val error = result as ToolCallResult.Error
        assertTrue(error.reason.contains("Missing 'tool'"))
    }
}
