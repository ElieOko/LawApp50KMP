package emy.partners.lawapp.data

import emy.partners.lawapp.domain.models.Article
import emy.partners.lawapp.domain.models.Comment
import emy.partners.lawapp.domain.models.ExtraContent
import emy.partners.lawapp.domain.models.User
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.one
import lawapp.shared.generated.resources.preview

object Constants {
    var comment1  = listOf<Comment>(
        Comment(1, "Très bon contenu, merci pour le partage !", User(1, "ElieOko", "")),
        Comment(2, "Je ne comprends pas trop cette partie ", User(2, "AminaDev", "")),
        Comment(3, "Wow c’est super bien expliqué ", User(3, "JeanTech", "")),
        Comment(4, "Est-ce que tu peux donner plus de détails ?", User(4, "SaraCode", "")),
        Comment(5, "Intéressant mais il manque des exemples", User(5, "MoussaDev", "")),
        Comment(6, "J’ai appris quelque chose aujourd’hui ", User(6, "NadiaUI", "")),
        Comment(7, "Franchement c’est clair et précis", User(7, "KevinK", "")),
        Comment(8, "Merci beaucoup pour ce post utile ", User(8, "LinaFlow", ""))
    )
    val comment2 = listOf<Comment>(
        Comment(1, "Le montage est propre, j’aime bien ", User(1, "ElieOko", "")),
        Comment(2, "Attends je suis perdu sur l’explication là ", User(2, "AminaDev", "")),
        Comment(3, "Ça mérite plus de visibilité franchement", User(3, "JeanTech", "")),
        Comment(4, "Tu peux faire une partie 2 stp ?", User(4, "SaraCode", "")),
        Comment(5, "Bonne idée mais j’aurais structuré autrement", User(5, "MoussaDev", "")),
        Comment(6, "Simple et efficace  merci", User(6, "NadiaUI", "")),
        Comment(7, "C’est exactement ce que je cherchais ", User(7, "KevinK", "")),
        Comment(8, "Incroyable, continue comme ça ", User(8, "LinaFlow", ""))
    )
    val comment3 = listOf<Comment>(
        Comment(1, "J’aime bien l’approche, c’est original ", User(1, "ElieOko", "")),
        Comment(2, "Quelqu’un peut m’expliquer la partie du milieu ? ", User(2, "AminaDev", "")),
        Comment(3, "C’est propre mais ça va un peu trop vite", User(3, "JeanTech", "")),
        Comment(4, "Franchement je ne m’attendais pas à ça ", User(4, "SaraCode", "")),
        Comment(5, "Tu devrais ajouter un exemple concret", User(5, "MoussaDev", "")),
        Comment(6, "Très utile pour les débutants ", User(6, "NadiaUI", "")),
        Comment(7, "Enfin quelqu’un qui explique clairement 💡", User(7, "KevinK", "")),
        Comment(8, "J’ai tout regardé sans pause, super intéressant ", User(8, "LinaFlow", ""))
    )
    val generateArticle = listOf<Article>(
        Article(
            1,
            "Roni",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent(),
            comments = comment1
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
            comments = comment2,
            isPlay = true,
            video = "https://cdn.pixabay.com/video/2016/09/21/5373-183629075_medium.mp4"
        ),
        Article(
            4,
            "Roni",
            profile = Res.drawable.one,
            "LawApp 50, vulgarisation du droit dans votre poche.",
            file = Res.drawable.preview,
            extra = ExtraContent(),
            comments = comment3,
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