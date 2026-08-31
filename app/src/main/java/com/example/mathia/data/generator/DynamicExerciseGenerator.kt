package com.example.mathia.data.generator

import kotlin.random.Random

object DynamicExerciseGenerator {

    fun generateQuestion(operation: String, level: Int = 1): Pair<String, Int> {
        val rand = Random(System.currentTimeMillis() + Random.nextLong(1, 10000))

        return when (operation) {
            "Suma" -> generateAddition(rand, level)
            "Resta" -> generateSubtraction(rand, level)
            "Multiplicacion" -> generateMultiplication(rand, level)
            "Division", "División" -> generateDivision(rand, level)
            "Fracciones" -> generateFractions(rand, level)
            "Series" -> generateSeries(rand, level)
            else -> generateAddition(rand, level)
        }
    }

    private fun generateAddition(rand: Random, level: Int): Pair<String, Int> {
        return when (level.coerceIn(1, 5)) {
            1 -> {
                val a = rand.nextInt(1, 10)
                val b = rand.nextInt(1, 10)
                Pair("$a + $b", a + b)
            }
            2 -> {
                val a = rand.nextInt(10, 50)
                val b = rand.nextInt(5, 45)
                Pair("$a + $b", a + b)
            }
            3 -> {
                val a = rand.nextInt(5, 15)
                val b = rand.nextInt(5, 15)
                val c = rand.nextInt(2, 12)
                Pair("$a + $b + $c", a + b + c)
            }
            4 -> {
                val base = rand.nextInt(1, 9) * 10
                val b = rand.nextInt(15, 85)
                Pair("$base + $b", base + b)
            }
            else -> {
                val names = listOf("Mateo", "Sofía", "Andree", "Lucas", "Valentina", "Thiago", "Camila")
                val items = listOf("estrellas", "galletas", "carritos", "globos", "estampas")
                val name = names.random(rand)
                val item = items.random(rand)
                val a = rand.nextInt(12, 35)
                val b = rand.nextInt(10, 30)
                Pair("¡$name juntó $a $item en la mañana y $b más por la tarde! ¿Cuántos tiene en total?", a + b)
            }
        }
    }

    private fun generateSubtraction(rand: Random, level: Int): Pair<String, Int> {
        return when (level.coerceIn(1, 5)) {
            1 -> {
                val a = rand.nextInt(5, 15)
                val b = rand.nextInt(1, a)
                Pair("$a - $b", a - b)
            }
            2 -> {
                val a = rand.nextInt(20, 60)
                val b = rand.nextInt(5, 25)
                Pair("$a - $b", a - b)
            }
            3 -> {
                val base = rand.nextInt(5, 9) * 10
                val minus = rand.nextInt(1, 4) * 10
                Pair("$base - $minus", base - minus)
            }
            4 -> {
                val a = rand.nextInt(45, 95)
                val b = rand.nextInt(18, a - 2)
                Pair("$a - $b", a - b)
            }
            else -> {
                val names = listOf("Axolita", "Andree", "Mateo", "Lucía", "Gael")
                val items = listOf("dulces", "canicas", "figuras", "estampas")
                val name = names.random(rand)
                val item = items.random(rand)
                val total = rand.nextInt(25, 60)
                val regala = rand.nextInt(8, total - 5)
                Pair("¡$name tenía $total $item y regaló $regala a sus amigos! ¿Cuántos le quedan?", total - regala)
            }
        }
    }

    private fun generateMultiplication(rand: Random, level: Int): Pair<String, Int> {
        return when (level.coerceIn(1, 5)) {
            1 -> {
                val a = listOf(2, 3, 5).random(rand)
                val b = rand.nextInt(2, 6)
                Pair("$a × $b", a * b)
            }
            2 -> {
                val a = listOf(4, 6, 7).random(rand)
                val b = rand.nextInt(3, 8)
                Pair("$a × $b", a * b)
            }
            3 -> {
                val a = rand.nextInt(6, 10)
                val b = rand.nextInt(4, 10)
                Pair("$a × $b", a * b)
            }
            4 -> {
                val factor = listOf(10, 20).random(rand)
                val n = rand.nextInt(3, 9)
                Pair("$n × $factor", n * factor)
            }
            else -> {
                val a = rand.nextInt(3, 7)
                val b = rand.nextInt(4, 9)
                val items = listOf("filas con $b ajolotes", "cajas con $b galletas", "bolsas con $b dulces")
                val item = items.random(rand)
                Pair("Si hay $a $item, ¿cuántos hay en total?", a * b)
            }
        }
    }

    private fun generateDivision(rand: Random, level: Int): Pair<String, Int> {
        return when (level.coerceIn(1, 5)) {
            1 -> {
                val divisor = listOf(2, 3).random(rand)
                val quotient = rand.nextInt(1, 6)
                val dividend = divisor * quotient
                Pair("$dividend ÷ $divisor", quotient)
            }
            2 -> {
                val divisor = listOf(2, 3, 4, 5).random(rand)
                val quotient = rand.nextInt(2, 8)
                val dividend = divisor * quotient
                Pair("$dividend ÷ $divisor", quotient)
            }
            3 -> {
                val divisor = rand.nextInt(6, 10)
                val quotient = rand.nextInt(2, 10)
                val dividend = divisor * quotient
                Pair("$dividend ÷ $divisor", quotient)
            }
            4 -> {
                val divisor = listOf(5, 10).random(rand)
                val quotient = rand.nextInt(3, 12)
                val dividend = divisor * quotient
                Pair("$dividend ÷ $divisor", quotient)
            }
            else -> {
                val names = listOf("Mateo", "Sofía", "Lucía", "Thiago", "Camila", "Valentino")
                val items = listOf("galletas", "estrellas", "canicas", "chocolates", "globos")
                val name = names.random(rand)
                val item = items.random(rand)
                val friends = rand.nextInt(2, 6)
                val perFriend = rand.nextInt(3, 9)
                val total = friends * perFriend
                Pair("¡$name tiene $total $item y desea repartirlos equitativamente entre $friends amigos! ¿Cuántos recibe cada uno?", perFriend)
            }
        }
    }

    private fun generateFractions(rand: Random, level: Int): Pair<String, Int> {
        return when (level.coerceIn(1, 5)) {
            1 -> {
                val num = rand.nextInt(1, 5)
                val den = rand.nextInt(num + 1, 9)
                Pair("¿Cuál es el numerador (arriba) de la fracción $num/$den?", num)
            }
            2 -> {
                val num = rand.nextInt(1, 4)
                val den = rand.nextInt(num + 1, 8)
                Pair("¿Cuál es el denominador (abajo) de la fracción $num/$den?", den)
            }
            3 -> {
                val total = listOf(4, 6, 8, 10, 12, 14, 16).random(rand)
                val parte = total / 2
                Pair("Si tienes $total tajadas de pastel y te comes la mitad (1/2), ¿cuántas te quedan?", parte)
            }
            4 -> {
                val total = listOf(8, 12, 16, 20, 24).random(rand)
                val num = total / 4
                Pair("¿Cuánto es 1/4 (la cuarta parte) de $total?", num)
            }
            else -> {
                val num1 = rand.nextInt(1, 4)
                val num2 = rand.nextInt(1, 4)
                val den = listOf(5, 6, 8, 10).random(rand)
                val suma = num1 + num2
                Pair("¿Cuánto es $num1/$den + $num2/$den? Ingresa el numerador resultante:", suma)
            }
        }
    }

    private fun generateSeries(rand: Random, level: Int): Pair<String, Int> {
        return when (level.coerceIn(1, 5)) {
            1 -> {
                val start = rand.nextInt(1, 8) * 2
                Pair("$start, ${start + 2}, ${start + 4}, ${start + 6}, ?", start + 8)
            }
            2 -> {
                val start = rand.nextInt(1, 6) * 5
                Pair("$start, ${start + 5}, ${start + 10}, ${start + 15}, ?", start + 20)
            }
            3 -> {
                val start = rand.nextInt(3, 8) * 10
                Pair("$start, ${start - 5}, ${start - 10}, ${start - 15}, ?", start - 20)
            }
            4 -> {
                val start = rand.nextInt(1, 6) * 3
                Pair("$start, ${start + 3}, ${start + 6}, ${start + 9}, ?", start + 12)
            }
            else -> {
                val start = rand.nextInt(2, 10) * 4
                Pair("$start, ${start + 4}, ${start + 8}, ${start + 12}, ?", start + 16)
            }
        }
    }

    fun obtenerPistaPedagogica(operation: String, questionText: String, attempts: Int, level: Int): String {
        val op = com.example.mathia.AdaptiveEngine.normalizarTema(operation)
        return when (op) {
            "Suma" -> {
                if (attempts <= 1) "Pista: Suma primero las unidades y luego las decenas."
                else "Recuerda: Cuenta hacia adelante agregando los números uno a uno."
            }
            "Resta" -> {
                if (attempts <= 1) "Pista: Piensa cuánto le falta al número menor para llegar al mayor."
                else "Recuerda: Si es necesario, descompón una decena en 10 unidades."
            }
            "Multiplicacion" -> {
                if (attempts <= 1) "Pista: La multiplicación es sumar un mismo número varias veces."
                else "Recuerda: Revisa la tabla del multiplicador o suma en grupos."
            }
            "Division" -> {
                if (attempts <= 1) "Pista: Piensa qué número multiplicado por el divisor da el dividendo."
                else "Recuerda: Reparte la cantidad total en partes iguales."
            }
            "Fracciones" -> {
                if (attempts <= 1) "Pista: El numerador (arriba) indica cuántas partes tomas; el denominador (abajo) en cuántas partes se divide el todo."
                else "Recuerda: La mitad es dividir entre 2 y la cuarta parte entre 4."
            }
            "Series" -> {
                if (attempts <= 1) "Pista: Observa de cuánto en cuánto aumentan o disminuyen los números."
                else "Recuerda: Calcula la diferencia entre el primer y segundo número."
            }
            else -> "Pista: Lee con atención el problema y resuelve paso a paso."
        }
    }
}
