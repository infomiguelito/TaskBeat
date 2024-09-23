package com.devspace.taskbeats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionScene.Transition.TransitionOnClick
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()
    private var tasks = listOf<TaskUiData>()
    private var categoriesEntity = listOf<CategoryEntity>()
    private val categoryAdapter = CategoryListAdapter()
    private val taskAdapter by lazy {
        TaskListAdapter()
    }


    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskDataBase::class.java, "database-task-beat-2"
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
        val fabCreateTask = findViewById<FloatingActionButton>(R.id.fab_create_task)

        fabCreateTask.setOnClickListener {
            showCreateUpdateTaskBottomSheet()

        }

        taskAdapter.setOnClickListener { task ->
            showCreateUpdateTaskBottomSheet(task)

        }


        categoryAdapter.setOnLongClickListener { categoryToBeDelete ->

            if(categoryToBeDelete.name != "+" && categoryToBeDelete.name != "All") {
                val title: String = this.getString(R.string.category_delete_title)
                val description: String = this.getString(R.string.category_delete_description)
                val btnText: String = this.getString(R.string.delete)

                showInfoDialog(
                    title,
                    description,
                    btnText
                ) {

                    val categoryEntityToBeDeleted = CategoryEntity(
                        categoryToBeDelete.name,
                        categoryToBeDelete.isSelected
                    )
                    deleteCategory(categoryEntityToBeDeleted)

                }
            }

        }

        categoryAdapter.setOnClickListener { selected ->
            if (selected.name == "+") {
                val createBottomSheet = CreateOrUpdateBottomSheet { categoryName ->
                    val categoryEntity = CategoryEntity(
                        name = categoryName,
                        isSelected = false
                    )
                    insertCategory(categoryEntity)

                }

                createBottomSheet.show(supportFragmentManager, "createBottomSheet")
            } else {
                val categoryTemp = categories.map { item ->
                    when {
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = true)
                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)
                        item.name != selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }

                    if (selected.name != "All") {
                        filterTaskByCategoryName(selected.name)
                    } else {
                        GlobalScope.launch(Dispatchers.IO) {
                        getTaskFromTaskDataBase()
                        }
                    }
                categoryAdapter.submitList(categoryTemp)
            }


        }

        rvCategory.adapter = categoryAdapter
        GlobalScope.launch(Dispatchers.IO) {
            getCategoriesFromDataBase()
        }


        rvTask.adapter = taskAdapter
        GlobalScope.launch(Dispatchers.IO) {
            getTaskFromTaskDataBase()

        }


    }


    private fun showInfoDialog(
        title: String,
        description: String,
        btnText: String,
        onClick: () -> Unit
    ) {
        val infoBottomSheet = InfoBottomSheet(
            title = title,
            description = description,
            btnText = btnText,
            onClick
        )

        infoBottomSheet.show(
            supportFragmentManager,
            "infoBottomSheet"
        )


    }

    private fun getCategoriesFromDataBase() {
        val categoriesFromDb: List<CategoryEntity> = categoryDao.getAll()
        categoriesEntity = categoriesFromDb
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
        val categoryListTemp = mutableListOf(CategoryUiData(
            name = "All",
            isSelected = true,)
        )
        categoryListTemp.addAll(categoriesUiData)

        GlobalScope.launch(Dispatchers.Main) {
            categories = categoryListTemp
            categoryAdapter.submitList(categories)
        }


    }

    private fun getTaskFromTaskDataBase() {

        val taskFromDb: List<TaskEntity> = taskDao.getAll()
        val taskUiData = taskFromDb.map {
            TaskUiData(
                id = it.id,
                name = it.name,
                category = it.category

            )
        }
        GlobalScope.launch(Dispatchers.Main) {
            tasks = taskUiData

            taskAdapter.submitList(taskUiData)
        }


    }

    private fun insertCategory(categoryEnity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insert(categoryEnity)
            getCategoriesFromDataBase()
        }

    }

    private fun insertTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insert(taskEntity)
            getTaskFromTaskDataBase()
        }
    }

    private fun updateTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.update(taskEntity)
            getTaskFromTaskDataBase()
        }
    }

    private fun deleteTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.delete(taskEntity)
            getTaskFromTaskDataBase()
        }
    }

    private fun deleteCategory(categoryEnity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksToBeDelete = taskDao.getAllByCategoryName(categoryEnity.name)
            taskDao.deleteAll(tasksToBeDelete)
            categoryDao.delete(categoryEnity)
            getCategoriesFromDataBase()
            getTaskFromTaskDataBase()
        }
    }

    private fun filterTaskByCategoryName(category : String){
        GlobalScope.launch(Dispatchers.IO) {
            val taskFromDb: List<TaskEntity> = taskDao.getAllByCategoryName(category)
            val taskUiData = taskFromDb.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category

                )
            }
            GlobalScope.launch(Dispatchers.Main) {
                taskAdapter.submitList(taskUiData)
            }
        }
    }


    private fun showCreateUpdateTaskBottomSheet(taskUiData: TaskUiData? = null) {
        val createTaskBottomSheet = CreateOrUpdateTaskBottomSheet(
            task = taskUiData,
            categoryList = categoriesEntity,
            onCreateClicked = { taskToBeCreated ->
                val taskEntityToBeInsert = TaskEntity(
                    name = taskToBeCreated.name,
                    category = taskToBeCreated.category
                )
                insertTask(taskEntityToBeInsert)
            },
            onUpdateClicked = { taskToBeUpdate ->
                val taskEntityToBeUpdate = TaskEntity(
                    id = taskToBeUpdate.id,
                    name = taskToBeUpdate.name,
                    category = taskToBeUpdate.category
                )
                updateTask(taskEntityToBeUpdate)

            },
            onDeleteClicked = { taskToBeDelete ->
                val taskEntityToBeDelete = TaskEntity(
                    id = taskToBeDelete.id,
                    name = taskToBeDelete.name,
                    category = taskToBeDelete.category
                )
                deleteTask(taskEntityToBeDelete)

            }
        )
        createTaskBottomSheet.show(
            supportFragmentManager,
            "createTaskBottomSheet"
        )
    }

}





