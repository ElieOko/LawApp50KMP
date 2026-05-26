package emy.partners.lawapp.data

import emy.partners.lawapp.domain.models.Article
import emy.partners.lawapp.domain.models.ExtraContent
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.one
import lawapp.shared.generated.resources.preview

object Constants {
    val generateArticle = listOf<Article>(
        Article(
            1,
            "Roni",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent()
        ),
        Article(
            2,
            "Roni",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent(),
            isPlay = false
        ),
        Article(
            3,
            "King Dav",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent(),
            isPlay = true,
            video = "https://cdn.pixabay.com/video/2016/09/21/5373-183629075_medium.mp4"
        ),
        Article(
            4,
            "Roni",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent()
        ),
        Article(
            5,
            "Roni",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.one,
            extra = ExtraContent(),
            isPlay = true,
            video = "https://filesamples.com/samples/video/mp4/sample_640x360.mp4"
        ),
        Article(
            6,
            "Hilaire",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent()
        ),
        Article(
            7,
            "Emy Mayumbi",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent(),
            isPlay = true,
            video = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
        ),
        Article(
            8,
            "Roni",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent()
        ),
        Article(
            9,
            "Roni",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent()
        ),

    )
}