package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        nomeUsuario=findViewById(R.id.et_nome_usuario)
        listaRepositories=findViewById(R.id.rv_lista_repositories)
        btnConfirmar=findViewById(R.id.btn_confirmar)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener{
            val userName=nomeUsuario.text.toString()
            saveUserLocal(userName)
            showUserName()
            getAllReposByUserName()
        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal(userName:String) {
        val sharedPrefs=getPreferences(Context.MODE_PRIVATE)
        with(sharedPrefs.edit()){
            putString(getString(R.string.key_user_name), userName)
            apply()
        }
    }

    private fun showUserName() {
        val sharedPrefs=getPreferences(Context.MODE_PRIVATE)
        val userName=sharedPrefs.getString(getString(R.string.key_user_name),"")
        nomeUsuario.setText(userName)
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        val retrofit = Retrofit
            .Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        githubApi=retrofit.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        val userName=nomeUsuario.text.toString()
        githubApi.getAllRepositoriesByUser(userName).enqueue(object : Callback<List<Repository>>{
            override fun onResponse(
                call: Call<List<Repository>>,
                response: Response<List<Repository>>
            ) {
                if(response.isSuccessful) response.body()?.let {
                    setupAdapter(it)
                    Toast.makeText(baseContext, "$userName possui ${it.size} repositório(s)!", Toast.LENGTH_LONG).show()
                }
                else Toast.makeText(baseContext, "Erro ao recuperar dados!", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                Toast.makeText(baseContext, "Erro ao recuperar dados! Verifique seu nome de usuário e tente novamente.", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {

        val adapter = RepositoryAdapter(list)
        listaRepositories.adapter=adapter
        adapter.gitItemLister={
            openBrowser(it.htmlUrl)
        }
        adapter.btnShareLister={
            shareRepositoryLink(it.htmlUrl)
        }
    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - Colocar esse metodo no click do share item do adapter
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    // @Todo 12 - Colocar esse metodo no click item do adapter
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}