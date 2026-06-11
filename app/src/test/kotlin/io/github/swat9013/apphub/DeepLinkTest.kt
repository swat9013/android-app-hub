package io.github.swat9013.apphub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepLinkTest {

    @Test
    fun `ホスト部をパッケージ名として取り出す`() {
        assertEquals("com.todoist", DeepLink.extractPackageName("apphub://com.todoist"))
    }

    @Test
    fun `末尾スラッシュは無視する`() {
        assertEquals("com.todoist", DeepLink.extractPackageName("apphub://com.todoist/"))
    }

    @Test
    fun `クエリ文字列は無視する`() {
        assertEquals(
            "com.todoist",
            DeepLink.extractPackageName("apphub://com.todoist?from=todoist"),
        )
    }

    @Test
    fun `余分なパスはパッケージ名に含めない`() {
        assertEquals("com.todoist", DeepLink.extractPackageName("apphub://com.todoist/extra"))
    }

    @Test
    fun `ホスト部が空なら対象なし`() {
        assertNull(DeepLink.extractPackageName("apphub://"))
    }

    @Test
    fun `URI なし(ランチャー起動)なら対象なし`() {
        assertNull(DeepLink.extractPackageName(null))
    }
}
