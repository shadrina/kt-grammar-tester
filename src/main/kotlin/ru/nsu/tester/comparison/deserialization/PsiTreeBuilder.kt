package ru.nsu.tester.comparison.deserialization

object PsiTreeBuilder {
    private const val INDENT = "  "

    private fun String.parseLine() : PsiRule {
        return when {
            this.contains("empty list") -> PsiToken("", this.replace(Regex("[\\s+<>]"), ""))
            this.contains("PsiElement") -> {
                val type = Regex("\\((.+?)\\)").find(this)?.groupValues?.get(1) ?: ""
                val token = Regex("\\('(.+?)'\\)").find(this)?.groupValues?.get(1) ?: ""
                PsiToken(type, token)
            }
            else -> PsiRule(this.replace(Regex("[\\s+0-9,()]"), ""))
        }
    }

    fun build(lines: List<String>) : PsiRule {
        val root = PsiRule("KtFile")
        var currNode = root
        currNode.parent = currNode

        var prevIndent = 0
        for (line in lines.subList(1, lines.lastIndex + 1)) {
            val currIndent = (line.length - line.replace(INDENT, "").length) / INDENT.length
            val element = line.parseLine()

            for (i in currIndent - prevIndent..0) currNode = currNode.parent
            currNode.addChild(element)
            currNode = element
            prevIndent = currIndent
        }

        return root
    }
}