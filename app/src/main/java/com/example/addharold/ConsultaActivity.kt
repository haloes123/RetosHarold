package com.example.addharold

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_consulta.*

class ConsultaActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    //Creo las variables que voy a usar.
    private var listaObjetosTemp: MutableList<Ciudades> = mutableListOf() //Lista temporal que se limpia despues de cambiar la ciudad
    private var listaObjetosPais: MutableList<pais> = mutableListOf() //Lista de objetos pais
    private var listaObjetosCiudad: MutableList<Ciudades> = mutableListOf() //Lista de objetos ciudad
    private var db = FirebaseFirestore.getInstance() //Crea la instancia a Firebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta)

        cargarPaises() //Llamada a la funcion cargarPaises

    }

    fun cargarPaises() {
        var listaPaises: MutableList<String> = mutableListOf() //Creo una lista de strings para rellenar el spinner

        db.collection("paises").get().addOnSuccessListener { //Hago la consulta a firebase
            result ->
            for (document in result){

                listaObjetosPais.add(pais(document.id, document.data["nombre"].toString(), //Creo los objetos de tipo pais y los meto en un array
                document.data["poblacion"].toString()))

                listaPaises.add(document.data["nombre"].toString()) //Guardo los nombres de todos los paises
            }
            adaptador(listaPaises) //Le paso el array de Strings con los nombres de los paises al adaptador
        }
    }

    fun adaptador(ListaPaises : MutableList<String>){ //Creacion del adaptador del 1er spinner
        var adaptadorPais = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, ListaPaises)
        spPaises?.adapter=adaptadorPais
        spPaises?.onItemSelectedListener = this
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { //Uso el OnItemSelected para cada pais usando los if para diferenciar cada Spinner
        if (p0!!.id == spPaises.id ){
            var posicionPais = spPaises.selectedItemPosition //Guardo la posicion del pais


            spPaises.setSelection(posicionPais) //Hago que se seleccione la posicionPais

            var listaNombreCiudades: MutableList<String> = mutableListOf() //Creo una lista guardando los nombres la ciudad
            db.collection("ciudades").get().addOnSuccessListener { //Hago la llamada a la base de datos para guardar los strings del nombre y los objetos de ciudades
                    result ->
                for (document in result){
                    if (document.data["cod_pais"] == listaObjetosPais[posicionPais].id){
                        listaNombreCiudades.add(document.data["nombre"].toString())
                        listaObjetosTemp.add(Ciudades(document.id, document.data["cod_pais"].toString(),
                            document.data["nombre"].toString(), document.data["poblacion"].toString()))
                    }
                }

                adaptador2(listaNombreCiudades) //Le paso la lista de strings al adaptador
            }
            tvPoblacionResultado.setText(listaObjetosPais[posicionPais].poblacion) //Printeo el texto de la poblacion del pais al TextView correspondiente
            listaObjetosCiudad = listaObjetosTemp //Copio la lista de objetos temporal a objetos ciudad
            listaObjetosTemp.clear() //Borro la lista de objetos temporal para que se vayan añadiendo
        }


        var poblacionPais = listaObjetosPais[p2].poblacion.toDouble() //Creo la variable donde voy a guardar la poblacion del pais para operar

        if (p0.id == spCiudades.id){ //Se ejecuta con el 2do Spinner
            var posicionCiudad = spCiudades.selectedItemPosition //Guardo la posicion seleccionada
            var listaObjetosMonumentos: MutableList<monumento> = mutableListOf() //Creo una lista de objetos monumento

            db.collection("monumentos").get().addOnSuccessListener { //Hago la llamada a la base de datos para obtener los monumentos
                    result ->
                for (document in result){
                    if (document.data["cod_poblacion"] == listaObjetosCiudad[posicionCiudad].id){
                        listaObjetosMonumentos.add(monumento(document.id,document["cod_poblacion"].toString(),
                        document["nombre"].toString()))
                    }
                    tvMonumentosResultado.setText("") //Vacio el Textview para colocar los nuevos monumentos
                }
                for (i in 0 until listaObjetosMonumentos.size){ //Recorro el array de objetos de tipo monumento y los añado al Textview para mostrarlo
                    tvMonumentosResultado.append(listaObjetosMonumentos[i].nombre + "\n")
                }
            }

            var poblacionCiudad = listaObjetosCiudad[posicionCiudad].poblacion.toDouble() //Casteo la poblacion de la ciudad a una variable

            tvCiudadResultado.setText(listaObjetosCiudad[posicionCiudad].nombre) //Printeo el nombre de la ciudad en un TextView
            val porcentaje = (poblacionCiudad/poblacionPais) * 100  //Hago la operacion para calcular el porcentaje de ciudad
            val numero3digitos:Double = Math.round(porcentaje * 1000.0) / 1000.0 //Redondeo el decimal y quito cifra de este
            val numero2digitos:Double = Math.round(numero3digitos * 100.0) / 100.0  //Vuelvo a hacerlo para quitar otra cifra
            val numero1digito:Double = Math.round(numero2digitos * 10.0) / 10.0 //Vuelvo a hacerlo para quitar otra mas y dejarlo con 1 decimal
            tvPorcentajeResultado.setText( numero1digito.toString() + "%")
            tvHabitantesresultado.setText(listaObjetosCiudad[posicionCiudad].poblacion) //Printeo los habitantes

        }
    }

    fun adaptador2(listaNombreCiudades: MutableList<String>){ //Creo el adaptador del 2do Spinner
        var adaptadorCiudades = ArrayAdapter(this,
        android.R.layout.simple_spinner_item,listaNombreCiudades)
        spCiudades?.adapter=adaptadorCiudades
        spCiudades?.onItemSelectedListener = this

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}