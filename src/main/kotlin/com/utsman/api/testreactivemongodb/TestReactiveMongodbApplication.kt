package com.utsman.api.testreactivemongodb

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.web.bind.annotation.*
import java.util.*

@SpringBootApplication
class TestReactiveMongodbApplication

fun main(args: Array<String>) {
	runApplication<TestReactiveMongodbApplication>(*args)
}

@Document(value = "name")
data class Name(
		@Id
		var id: String? = null,
		val name: String,
		val region: String
)

data class Responses(
		var size: Int? = null,
		val data: Any
)

interface NameRepository : MongoRepository<Name, String>

@RestController
@RequestMapping("/api")
class NameController {
	@Autowired
	lateinit var repository: NameRepository

	@RequestMapping(value = ["/"], method = [RequestMethod.GET])
	fun getAllName() : Observable<Responses> {
		return Observable.just(repository)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.single())
				.map {
					val result = it.findAll()
					return@map Responses(result.size, result)
				}
	}

	@RequestMapping(value = ["/"], method = [RequestMethod.POST])
	fun postName(@RequestBody name: Name) : Observable<Responses> {
		name.id = UUID.randomUUID().toString()
		return Observable.just(repository)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.single())
				.map {
					val result = it.save(name)
					return@map Responses(1, result)
				}
	}

	@RequestMapping(value = ["/all/"], method = [RequestMethod.POST])
	fun postAllName(@RequestBody names: List<Name>) : Observable<Responses> {
		return Observable.just(repository)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.single())
				.doOnNext {
					names.forEach { name ->
						name.id = UUID.randomUUID().toString()
					}
				}
				.map {
					val result = it.saveAll(names)
					return@map Responses(result.size, result)
				}
	}

	@RequestMapping(value = ["/{id}"], method = [RequestMethod.GET])
	fun getNameById(@PathVariable("id") id: String) : Observable<Responses> {
		return Observable.just(repository)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.single())
				.map {
					val result = it.findById(id).get()
					return@map Responses(1, listOf(result))
				}
	}

	@RequestMapping(value = ["/{id}"], method = [RequestMethod.DELETE])
	fun deleteNameById(@PathVariable("id") id: String) : Observable<Responses> {
		return Observable.just(repository)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.single())
				.map {
					val result = it.deleteById(id)
					return@map Responses(0, result)
				}
	}

	@RequestMapping(value = ["/"], method = [RequestMethod.DELETE])
	fun deleteNameAll() : Observable<Responses> {
		return Observable.just(repository)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.single())
				.map {
					val result = it.deleteAll()
					return@map Responses(1, result)
				}
	}
}
