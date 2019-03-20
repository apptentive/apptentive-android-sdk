package apptentive.com.android.movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class MovieViewModel(
    application: Application,
    repository: MovieRepository
) : AndroidViewModel(application) {
    val movies: LiveData<Array<Movie>> = repository.movies
}
