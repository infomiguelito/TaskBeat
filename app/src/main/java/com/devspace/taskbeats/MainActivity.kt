package com.devspace.taskbeats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()

    private var tasks = listOf<TaskUiData>()

    private val db by lazy  {
        Room.databaseBuilder(
        applicationContext,
        TaskBeatDataBase::class.java, "database-taskbeat"
    ).build()
    }

    private val categoryDao: CategoryDao by lazy {
        db.getCategoryDao()
    }

    private val taskDao: TaskDao by lazy {
        db.getTaskDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val rvCategory = findViewById<RecyclerView>(R.id.rv_categories)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)

        val taskAdapter = TaskListAdapter()
        val categoryAdapter = CategoryListAdapter()


        categoryAdapter.setOnClickListener { selected ->
            if (selected.name == "+"){
                Snackbar.make(rvCategory,"+ funciona",Snackbar.LENGTH_LONG).show()}
            else {
                val categoryTemp = categories.map { item ->
                    when {
                        item.name == selected.name && !item.isSelected && item.name != "+"-> item.copy(isSelected = true)
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }



             val taskTemp =
                if (selected.name != "ALL" && selected.name != "+") {
                    tasks.filter { it.category == selected.name }
                } else {
                    tasks
                }
                taskAdapter.submitList(taskTemp)

                categoryAdapter.submitList(categoryTemp)
            }
        }

        rvCategory.adapter = categoryAdapter
        getCategoriesFromDataBase(categoryAdapter)

        rvTask.adapter = taskAdapter
        getTaskFromDataBase(taskAdapter)


    }





    private fun getCategoriesFromDataBase(adapter: CategoryListAdapter){
        GlobalScope.launch(Dispatchers.IO){
            val categoriesFromDb: List<CategoryEnity> = categoryDao.getAll()
            val categoriesUiData = categoriesFromDb.map {
                CategoryUiData(
                    name = it.name,
                    isSelected = it.isSelected
                )
            }
                .toMutableList()
            categoriesUiData.add(
                CategoryUiData(
                    name = "+",
                    isSelected = false
                )
            )
            categories = categoriesUiData

            adapter.submitList(categoriesUiData)
        }
    }

    private fun getTaskFromDataBase(adapter: TaskListAdapter){
        GlobalScope.launch(Dispatchers.IO){
            val taskFromDb = taskDao.getAll()
            val taskUiData = taskFromDb.map {
                TaskUiData(
                    category = it.category,
                    name = it.name

                )
            }

            tasks = taskUiData

            adapter.submitList(taskUiData)
        }

    }




}



