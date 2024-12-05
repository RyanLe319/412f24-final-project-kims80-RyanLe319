package com.example.smithsonian

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SmithsonianApiTest {
    @Test
    fun searchGeneralTest() {
        val objList = SmithsonianApi.searchGeneral("art")
        val obj = objList[0]
        assert(obj.id == "ld1-1643399756728-1643399815679-0")
        assert(obj.title == "Society of American Artists, Jury of 1890")
        assert(obj.image == "https://ids.si.edu/ids/deliveryService?id=NPG-NPG_2009_7M-000001")
        assert(obj.date == "1890")
        assert(obj.name == "Unidentified Artist")
    }

    @Test
    fun searchCategoryTest() {
        val objList = SmithsonianApi.searchCategory("art", "art_design")
        val obj = objList[0]
        assert(obj.id == "ld1-1643399756728-1643399815679-0")
        assert(obj.title == "Society of American Artists, Jury of 1890")
        assert(obj.image == "https://ids.si.edu/ids/deliveryService?id=NPG-NPG_2009_7M-000001")
        assert(obj.date == "1890")
        assert(obj.name == "Unidentified Artist")
    }

    @Test
    fun searchTermsTest() {
        val objList = SmithsonianApi.searchTerms("date")
        assert(objList[0] == "0-00s")
        assert(objList[1] == "0?00s")
        assert(objList[2] == "1000s")
        assert(objList[3] == "100s")
        assert(objList[4] == "1100s")
    }
}