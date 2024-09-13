package com.devspace.taskbeats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()

    private var tasks = listOf<TaskUiData>()

    private val categoryAdapter = CategoryListAdapter()


    private val db by lazy {
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
        val fabCreatTask = findViewById<FloatingActionButton>(R.id.fab_create_task)

        fabCreatTask.setOnClickListener {
            val createTaskBottomSheet = TaskBottomSheet(
            categories
            ){taskToBeCreated ->

            }
            createTaskBottomSheet.show(
                supportFragmentManager,
                "createTaskBottomSheet"
            )
        }

        val taskAdapter = TaskListAdapter()


        categoryAdapter.setOnClickListener { selected ->
            if (selected.name == "+") {
                val createBottomSheet = BottomSheet { categoryName ->
                    val categoryEntity = CategoryEnity(
                        name = categoryName,
                        isSelected = false
                    )
                    insertCategory(categoryEntity)

                }

                createBottomSheet.show(supportFragmentManager, "createBottomSheet")
            } else {
                val categoryTemp = categories.map { item ->
                    when {
                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }

                val taskTemp =
                    if (selected.name != "ALL") {
                        tasks.filter { it.category == selected.name }
                    } else {
                        tasks
                    }
                taskAdapter.submitList(taskTemp)

                categoryAdapter.submitList(categoryTemp)
            }


        }

        rvCategory.adapter = categoryAdapter
        GlobalScope.launch(Dispatchers.IO) {
            getCategoriesFromDataBase()
        }


        rvTask.adapter = taskAdapter
        getTaskFromDataBase(taskAdapter)


    }


    private fun getCategoriesFromDataBase() {
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
            GlobalScope.launch(Dispatchers.Main) {
                categories = categoriesUiData
                    categoryAdapter.submitList(categoriesUiData)
            }


    }

    private fun getTaskFromDataBase(adapter: TaskListAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            val taskFromDb: List<TaskEntity> = taskDao.getAll()
            val taskUiData = taskFromDb.map {
                TaskUiData(
                    category = it.category,
                    name = it.name

                )
            }
            GlobalScope.launch(Dispatchers.Main) {
                tasks = taskUiData

                adapter.submitList(taskUiData)
            }


        }

    }
    private fun insertCategory(categoryEnity: CategoryEnity){
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.inset(categoryEnity )
            getCategoriesFromDataBase()
        }

    }
}





