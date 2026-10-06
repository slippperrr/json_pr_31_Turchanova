package com.example.turchanova_json

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etName = findViewById<EditText>(R.id.etName)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val etTags = findViewById<EditText>(R.id.etTags)
        val btnHandmade = findViewById<Button>(R.id.btnHandmade)
        val btnFromInput = findViewById<Button>(R.id.btnFromInput)
        val btnSelfCheck = findViewById<Button>(R.id.btnSelfCheck)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnHandmade.setOnClickListener {
            val product = Product(
                name = "Турчанова Дарья, программист",
                price = 150000.0,
                tags = listOf("учебник", "программирование", "android")
            )

            // СЕРИАЛИЗАЦИЯ
            val jsonString = productToJson(product)

            //  ДЕСЕРИАЛИЗАЦИЯ
            val restored = jsonToProduct(jsonString)

            val result = StringBuilder()
            result.append("1: Объект, созданный руками\n\n")
            result.append(" Исходный объект\n")
            result.append("Название: ${product.name}\n")
            result.append("Цена: ${product.price}\n")
            result.append("Теги: ${product.tags}\n\n")
            result.append("JSON-строка\n")
            result.append("$jsonString\n\n")
            result.append("Восстановленный объект\n")
            result.append("Название: ${restored.name}\n")
            result.append("Цена: ${restored.price}\n")
            result.append("Теги: ${restored.tags}\n")

            tvResult.text = result.toString()
            Log.d("MyApp", result.toString())
        }

        btnFromInput.setOnClickListener {
            val name = etName.text.toString().ifBlank { "Без названия" }
            val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val tags = etTags.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val product = Product(name, price, tags)

            val jsonString = productToJson(product)
            val restored = jsonToProduct(jsonString)

            val result = StringBuilder()
            result.append(" 2: Объект из полей ввода \n\n")
            result.append("Исходный объект \n")
            result.append("Название: ${product.name}\n")
            result.append("Цена: ${product.price}\n")
            result.append("Теги: ${product.tags}\n\n")
            result.append("JSON-строка \n")
            result.append("$jsonString\n\n")
            result.append(" Восстановленный объект \n")
            result.append("Название: ${restored.name}\n")
            result.append("Цена: ${restored.price}\n")
            result.append("Теги: ${restored.tags}\n")

            tvResult.text = result.toString()
            Log.d("MyApp", result.toString())
        }

        btnSelfCheck.setOnClickListener {
            val result = StringBuilder()
            result.append("3: Самопроверка ошибок\n\n")

            // 1: нет поля price
            result.append("1: JSON без поля \"price\" \n")
            val jsonWithoutPrice = """{"name":"Книга","tags":["учебник","android"]}"""
            result.append("Входной JSON: $jsonWithoutPrice\n")

            try {
                val p1 = jsonToProduct(jsonWithoutPrice)
                result.append("Результат: name=${p1.name}, price=${p1.price}, tags=${p1.tags}\n")
                result.append("Вывод: мы сами задали значение по умолчанию 0.0\n\n")
            } catch (e: Exception) {
                result.append("Ошибка: ${e.message}\n\n")
            }

            // 2: лишнее поле author
            result.append("2: JSON с лишним полем \"author\" \n")
            val jsonWithExtra =
                """{"name":"Книга","price":1500.0,"tags":["учебник"],"author":"Иванов"}"""
            result.append("Входной JSON: $jsonWithExtra\n")

            try {
                val p2 = jsonToProduct(jsonWithExtra)
                result.append("Результат: name=${p2.name}, price=${p2.price}, tags=${p2.tags}\n")
                result.append("Вывод: лишнее поле \"author\" мы просто не читаем — оно игнорируется\n\n")
            } catch (e: Exception) {
                result.append("Ошибка: ${e.message}\n\n")
            }

            // 3: число в кавычках ---
            result.append("Опыт 3: число в кавычках (\"1500\")\n")
            val jsonWrongType = """{"name":"Книга","price":"1500","tags":["учебник"]}"""
            result.append("Входной JSON: $jsonWrongType\n")

            try {
                val p3 = jsonToProduct(jsonWrongType)
                result.append("Результат: price=${p3.price}\n")
                result.append("Вывод: optDouble смог преобразовать строку \"1500\" в Double\n\n")
            } catch (e: Exception) {
                result.append("Ошибка: ${e.message}\n\n")
            }

            // 4: пустой JSON ---
            result.append("4: пустой JSON {}\n")
            val jsonEmpty = """{}"""
            result.append("Входной JSON: $jsonEmpty\n")

            try {
                val p4 = jsonToProduct(jsonEmpty)
                result.append("Результат: name=${p4.name}, price=${p4.price}, tags=${p4.tags}\n")
                result.append("Вывод: optString вернул \"\", optDouble вернул 0.0, массив пустой\n")
            } catch (e: Exception) {
                result.append("Ошибка: ${e.message}\n")
            }

            tvResult.text = result.toString()
            Log.d("MyApp", result.toString())
        }
    }
    private fun productToJson(product: Product): String {
        val jsonObject = JSONObject()

        jsonObject.put("name", product.name)
        jsonObject.put("price", product.price)

        val tagsArray = JSONArray()
        for (tag in product.tags) {
            tagsArray.put(tag)
        }
        jsonObject.put("tags", tagsArray)

        return jsonObject.toString()
    }
    private fun jsonToProduct(jsonString: String): Product {
        val jsonObject = JSONObject(jsonString)

        val name = jsonObject.optString("name", "")
        val price = jsonObject.optDouble("price", 0.0)

        val tagsList = mutableListOf<String>()
        val tagsArray = jsonObject.optJSONArray("tags")
        if (tagsArray != null) {
            for (i in 0 until tagsArray.length()) {
                tagsList.add(tagsArray.optString(i, ""))
            }
        }
        return Product(name, price, tagsList)
    }
}
