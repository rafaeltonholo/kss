package dev.tonholo.kss.parser.ast.css

import dev.tonholo.kss.parser.ast.css.CssCombinator
import dev.tonholo.kss.parser.ast.css.syntax.node.Selector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CssSelectorParserTest {
    private val parser = CssSelectorParser()

    @Test
    fun `given type selector - when parse is called - then returns single SelectorListItem with Type`() {
        // Arrange
        val selector = "div"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val item = result.first()
        assertEquals(expected = 1, actual = item.selectors.size)
        val sel = assertIs<Selector.Type>(item.selectors.first())
        assertEquals(expected = "div", actual = sel.name)
    }

    @Test
    fun `given class selector - when parse is called - then returns single SelectorListItem with Class`() {
        // Arrange
        val selector = ".active"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val sel = assertIs<Selector.Class>(result.first().selectors.first())
        assertEquals(expected = "active", actual = sel.name)
    }

    @Test
    fun `given id selector - when parse is called - then returns single SelectorListItem with Id`() {
        // Arrange
        val selector = "#header"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val sel = assertIs<Selector.Id>(result.first().selectors.first())
        assertEquals(expected = "header", actual = sel.name)
    }

    @Test
    fun `given universal selector - when parse is called - then returns Type with asterisk name`() {
        // Arrange
        val selector = "*"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val sel = assertIs<Selector.Type>(result.first().selectors.first())
        assertEquals(expected = "*", actual = sel.name)
    }

    @Test
    fun `given comma-separated selectors - when parse is called - then returns multiple SelectorListItems`() {
        // Arrange
        val selector = "h1, h2, h3"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 3, actual = result.size)
        val names = result.map { item ->
            val sel = assertIs<Selector.Type>(item.selectors.first())
            sel.name
        }
        assertEquals(expected = listOf("h1", "h2", "h3"), actual = names)
    }

    @Test
    fun `given compound selector - when parse is called - then returns SelectorListItem with multiple selectors`() {
        // Arrange
        val selector = "div.active#main"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        assertEquals(expected = 3, actual = selectors.size)
        assertIs<Selector.Type>(selectors[0])
        assertIs<Selector.Class>(selectors[1])
        assertIs<Selector.Id>(selectors[2])
    }

    @Test
    fun `given descendant combinator - when parse is called - then selector has DescendantCombinator`() {
        // Arrange
        val selector = "div p"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        assertEquals(expected = 2, actual = selectors.size)
        assertEquals(expected = CssCombinator.DescendantCombinator, actual = selectors[0].combinator)
    }

    @Test
    fun `given child combinator - when parse is called - then selector has ChildCombinator`() {
        // Arrange
        val selector = "ul > li"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        assertEquals(expected = 2, actual = selectors.size)
        assertEquals(expected = CssCombinator.ChildCombinator, actual = selectors[0].combinator)
    }

    @Test
    fun `given attribute selector - when parse is called - then returns Attribute selector`() {
        // Arrange
        val selector = """[type="text"]"""

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val sel = assertIs<Selector.Attribute>(result.first().selectors.first())
        assertEquals(expected = "type", actual = sel.name)
        assertEquals(expected = "=", actual = sel.matcher)
        assertEquals(expected = "text", actual = sel.value)
    }

    @Test
    fun `given pseudo-class with parameters - when parse is called - then PseudoClass has parameters`() {
        // Arrange
        val selector = ":not(.hidden)"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val sel = assertIs<Selector.PseudoClass>(result.first().selectors.first())
        assertEquals(expected = "not", actual = sel.name)
        assertEquals(expected = 1, actual = sel.parameters.size)
        val inner = assertIs<Selector.Class>(sel.parameters.first())
        assertEquals(expected = "hidden", actual = inner.name)
    }

    @Test
    fun `given pseudo-element selector - when parse is called - then returns PseudoElement`() {
        // Arrange
        val selector = "p::first-line"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        assertEquals(expected = 2, actual = selectors.size)
        val pseudo = assertIs<Selector.PseudoElement>(selectors[1])
        assertEquals(expected = "first-line", actual = pseudo.name)
    }

    @Test
    fun `given empty string - when parse is called - then returns empty list`() {
        // Arrange
        val selector = ""

        // Act
        val result = parser.parse(selector)

        // Assert
        assertTrue(actual = result.isEmpty())
    }

    @Test
    fun `given blank string - when parse is called - then returns empty list`() {
        // Arrange
        val selector = "   "

        // Act
        val result = parser.parse(selector)

        // Assert
        assertTrue(actual = result.isEmpty())
    }

    @Test
    fun `given invalid selector - when parse is called - then returns empty list`() {
        // Arrange
        val selector = "{{{"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertTrue(actual = result.isEmpty())
    }

    @Test
    fun `given trailing combinator - when parse is called - then returns empty list`() {
        // Arrange
        val selector = "div >"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertTrue(actual = result.isEmpty())
    }

    @Test
    fun `given mixed comma-separated selectors - when parse is called - then returns all items`() {
        // Arrange
        val selector = "#id, .class, div"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 3, actual = result.size)
        assertIs<Selector.Id>(result[0].selectors.first())
        assertIs<Selector.Class>(result[1].selectors.first())
        assertIs<Selector.Type>(result[2].selectors.first())
    }

    @Test
    fun `given complex compound with attribute and pseudo - when parse is called - then returns full selector chain`() {
        // Arrange
        val selector = """input[type="text"]:focus"""

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        assertEquals(expected = 3, actual = selectors.size)
        assertIs<Selector.Type>(selectors[0])
        assertIs<Selector.Attribute>(selectors[1])
        assertIs<Selector.PseudoClass>(selectors[2])
    }

    @Test
    fun `given general sibling combinator - when parse is called - then selector has NextSiblingCombinator`() {
        // Arrange
        val selector = "h1 ~ p"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        assertEquals(expected = 2, actual = selectors.size)
        assertEquals(
            expected = CssCombinator.NextSiblingCombinator,
            actual = selectors[0].combinator
        )
    }

    @Test
    fun `given adjacent sibling combinator - when parse is called - then selector has SubsequentSiblingCombinator`() {
        // Arrange
        val selector = "h1 + p"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        assertEquals(expected = 2, actual = selectors.size)
        assertEquals(
            expected = CssCombinator.SubsequentSiblingCombinator,
            actual = selectors[0].combinator
        )
    }

    @Test
    fun `given selector with legacy deep combinator - when parse - then returns selector with DeepCombinator`() {
        // Arrange
        val selector = "html /deep/ [layout][vertical][reverse]"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        val html = assertIs<Selector.Type>(selectors[0])
        assertEquals(expected = "html", actual = html.name)
        assertEquals(expected = CssCombinator.DeepCombinator, actual = html.combinator)
        val attrs = selectors.drop(n = 1).map { assertIs<Selector.Attribute>(it).name }
        assertEquals(expected = listOf("layout", "vertical", "reverse"), actual = attrs)
    }

    @Test
    fun `given type-to-type deep combinator - when parse - then DeepCombinator links both types`() {
        // Arrange
        val selector = "html /deep/ body"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        assertEquals(expected = 2, actual = selectors.size)
        val html = assertIs<Selector.Type>(selectors[0])
        val body = assertIs<Selector.Type>(selectors[1])
        assertEquals(expected = "html", actual = html.name)
        assertEquals(expected = "body", actual = body.name)
        assertEquals(expected = CssCombinator.DeepCombinator, actual = html.combinator)
    }

    @Test
    fun `given universal to class deep combinator - when parse - then DeepCombinator links universal and class`() {
        // Arrange
        val selector = "* /deep/ .foo"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val selectors = result.first().selectors
        assertEquals(expected = 2, actual = selectors.size)
        val universal = assertIs<Selector.Type>(selectors[0])
        val cls = assertIs<Selector.Class>(selectors[1])
        assertEquals(expected = "*", actual = universal.name)
        assertEquals(expected = "foo", actual = cls.name)
        assertEquals(expected = CssCombinator.DeepCombinator, actual = universal.combinator)
    }

    @Test
    fun `given parsed deep combinator - when rendered via toString - then round-trips with spaced deep combinator`() {
        // Arrange
        val selector = "html/deep/body"

        // Act
        val rendered = parser.parse(selector).first().toString(indent = 0)

        // Assert
        // The pretty-printer normalizes the combinator to " /deep/ " regardless of
        // whether the input had surrounding whitespace.
        assertEquals(expected = "html /deep/ body", actual = rendered)
    }

    @Test
    fun `given presence attribute selector - when parse is called - then returns Attribute with null matcher`() {
        // Arrange
        val selector = "[disabled]"

        // Act
        val result = parser.parse(selector)

        // Assert
        assertEquals(expected = 1, actual = result.size)
        val sel = assertIs<Selector.Attribute>(result.first().selectors.first())
        assertEquals(expected = "disabled", actual = sel.name)
        assertEquals(expected = null, actual = sel.matcher)
        assertEquals(expected = null, actual = sel.value)
    }
}
