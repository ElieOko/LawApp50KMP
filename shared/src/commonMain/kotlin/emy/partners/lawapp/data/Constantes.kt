package emy.partners.lawapp.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import emy.partners.lawapp.domain.models.Article
import emy.partners.lawapp.domain.models.Blog
import emy.partners.lawapp.domain.models.Category
import emy.partners.lawapp.domain.models.Comment
import emy.partners.lawapp.domain.models.EvaluationSession
import emy.partners.lawapp.domain.models.EvaluationStatus
import emy.partners.lawapp.domain.models.ExtraContent
import emy.partners.lawapp.domain.models.QuizQuestion
import emy.partners.lawapp.domain.models.User
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.dr
import lawapp.shared.generated.resources.droit
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

    val blog = listOf<Blog>(
        Blog(
            id = 3,
            title = "Droit du numerique",
            background = Res.drawable.droit,
            description = "Comprendre les droits et obligations qui encadrent les activites numeriques, la protection des donnees et les preuves electroniques.",
            author = User(1, "Ethberg", ""),
            type = "Article"
        ),
        Blog(
            id = 4,
            title = "Droit moderne en RDC ",
            background = Res.drawable.dr,
            description = "Un panorama clair des nouvelles pratiques juridiques en RDC et des reflexes a adopter dans la vie quotidienne.",
            author = User(2, "EmyMayumbi", ""),
            type = "Article"
        ),
        Blog(
            id = 0,
            title = "LawApp50",
            background = Res.drawable.preview,
            description = "Les bases de LawApp50 pour apprendre le droit rapidement avec des fiches, des cas pratiques et des quiz.",
            author = User(3, "ElieOko", ""),
            type = "Article"
        ),
    )

    val evaluations = listOf(
        EvaluationSession(
            id = 1,
            title = "Diagnostic droit civil",
            domain = "Droit Civil",
            description = "Mesure ta comprehension des contrats, obligations et responsabilites civiles.",
            status = EvaluationStatus.InProgress,
            progress = 0.64f,
            score = null,
            questionCount = 25,
            completedQuestions = 16,
            duration = "18 min",
            updatedAt = "Aujourd'hui",
            level = "Intermediaire"
        ),
        EvaluationSession(
            id = 2,
            title = "Evaluation constitutionnelle",
            domain = "Droit Constitutionnel",
            description = "Revois les institutions, les droits fondamentaux et les principes de l'Etat.",
            status = EvaluationStatus.Completed,
            progress = 1f,
            score = 86,
            questionCount = 20,
            completedQuestions = 20,
            duration = "14 min",
            updatedAt = "Hier",
            level = "Avance"
        ),
        EvaluationSession(
            id = 3,
            title = "Cas pratique travail",
            domain = "Droit du Travail",
            description = "Teste tes reflexes sur le contrat de travail, les conges et les litiges employeur-salarie.",
            status = EvaluationStatus.InProgress,
            progress = 0.32f,
            score = null,
            questionCount = 18,
            completedQuestions = 6,
            duration = "12 min",
            updatedAt = "Il y a 2 jours",
            level = "Debutant"
        ),
        EvaluationSession(
            id = 4,
            title = "Revision procedure penale",
            domain = "Droit Penal",
            description = "Un parcours rapide sur les etapes de la procedure, les garanties et les recours.",
            status = EvaluationStatus.Completed,
            progress = 1f,
            score = 74,
            questionCount = 22,
            completedQuestions = 22,
            duration = "20 min",
            updatedAt = "Cette semaine",
            level = "Intermediaire"
        )
    )

    val quizQuestions = listOf(
        QuizQuestion(
            id = 1,
            title = "Quel element rend un contrat valable ?",
            category = "Droit Civil",
            options = listOf(
                "Le consentement libre des parties",
                "La signature d'un avocat",
                "La publication au journal officiel",
                "La presence obligatoire d'un temoin"
            ),
            correctIndex = 0,
            explanation = "Un contrat repose notamment sur le consentement libre et eclaire des parties, une capacite et un objet licite."
        ),
        QuizQuestion(
            id = 2,
            title = "Quel droit protege la personne contre une arrestation arbitraire ?",
            category = "Droit Constitutionnel",
            options = listOf(
                "La liberte d'association",
                "La surete individuelle",
                "Le droit de propriete",
                "La liberte du commerce"
            ),
            correctIndex = 1,
            explanation = "La surete individuelle protege contre les privations de liberte non prevues ou non controlees par la loi."
        ),
        QuizQuestion(
            id = 3,
            title = "Dans une relation de travail, le salaire est principalement :",
            category = "Droit du Travail",
            options = listOf(
                "Une faveur accordee par l'employeur",
                "Une sanction disciplinaire",
                "La contrepartie du travail fourni",
                "Un remboursement automatique des frais"
            ),
            correctIndex = 2,
            explanation = "Le salaire remunere le travail fourni et fait partie des obligations essentielles de l'employeur."
        )
    )
}