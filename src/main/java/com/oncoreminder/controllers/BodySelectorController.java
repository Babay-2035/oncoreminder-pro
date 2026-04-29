package com.oncoreminder.controllers;

import com.oncoreminder.models.Cancer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BodySelectorController implements Initializable {

    @FXML private TextField tfSearch, tfNom, tfOrgane;
    @FXML private ListView<String> listCancers;
    @FXML private Pane bodyPane;
    @FXML private Label lblTitle, lblSelectedZone;
    @FXML private ComboBox<String> cbClassification, cbStade;
    @FXML private TextArea taDescription, taSymptomes;
    @FXML private Button btnEnregistrer;

    private Consumer<Cancer> onSaveCallback;
    private boolean isEditMode = false;
    private int editId = 0;
    private boolean autoFilling = false;
    private String highlightedZone = null;

    private final Map<String, List<Shape>> zoneShapes = new LinkedHashMap<>();
    private final Map<String, String[]> zoneColors = new LinkedHashMap<>();

    // ═══════════════════════════════════════════════════════════════
    //  DONNÉES CANCERS COMPLÈTES (TOUS LES TYPES)
    // ═══════════════════════════════════════════════════════════════
    private static final List<CancerEntry> ALL_CANCERS = new ArrayList<>();
    static {
        // ═══════════════════════════════════════════════════════════
        //  CARCINOMES (épithéliaux)
        // ═══════════════════════════════════════════════════════════
        add("Cancer du sein", "Carcinome", "Sein", "sein",
                "Tumeur maligne du tissu mammaire, cancer le plus fréquent chez la femme.",
                "Boule dans le sein, modification de la peau, écoulement du mamelon, douleur.");
        add("Cancer du sein masculin", "Carcinome", "Sein", "sein",
                "Cancer rare du tissu mammaire chez l'homme, souvent diagnostiqué tardivement.",
                "Masse sous le mamelon, écoulement, rétraction du mamelon.");
        add("Cancer du poumon", "Carcinome", "Poumon", "poumon",
                "Tumeur broncho-pulmonaire souvent liée au tabagisme, principal cancer mortel.",
                "Toux persistante, essoufflement, douleur thoracique, crachats sanglants.");
        add("Cancer du poumon à petites cellules", "Carcinome", "Poumon", "poumon",
                "Forme agressive de cancer du poumon, fortement liée au tabagisme.",
                "Toux, douleur thoracique, essoufflement, syndrome cave supérieur.");
        add("Cancer du poumon non à petites cellules", "Carcinome", "Poumon", "poumon",
                "Forme la plus fréquente de cancer du poumon (80% des cas).",
                "Toux chronique, hémoptysie, douleur thoracique, perte de poids.");
        add("Adénocarcinome pulmonaire", "Carcinome", "Poumon", "poumon",
                "Sous-type de cancer du poumon fréquent chez les non-fumeurs.",
                "Toux, douleurs thoraciques, essoufflement, fatigue.");
        add("Cancer du côlon", "Carcinome", "Côlon", "colon",
                "Tumeur maligne du côlon, généralement précédée de polypes bénins.",
                "Changement du transit, sang dans les selles, douleurs abdominales.");
        add("Cancer colorectal", "Carcinome", "Côlon / Rectum", "colon",
                "Cancer touchant le côlon ou le rectum, l'un des plus fréquents.",
                "Sang dans les selles, douleurs abdominales, modification du transit.");
        add("Cancer de la prostate", "Carcinome", "Prostate", "pelvis",
                "Tumeur de la glande prostatique, cancer le plus fréquent chez l'homme après 50 ans.",
                "Difficultés à uriner, mictions fréquentes, douleurs pelviennes.");
        add("Cancer de l'ovaire", "Carcinome", "Ovaire", "pelvis",
                "Tumeur maligne des ovaires, souvent détectée tardivement.",
                "Ballonnements, douleurs pelviennes, troubles urinaires, perte d'appétit.");
        add("Cancer du col de l'utérus", "Carcinome", "Col de l'utérus", "pelvis",
                "Cancer développé au niveau du col utérin, principalement causé par le HPV.",
                "Saignements anormaux, pertes vaginales inhabituelles, douleurs pelviennes.");
        add("Cancer de l'utérus", "Carcinome", "Utérus", "pelvis",
                "Tumeur maligne de l'endomètre, survenant le plus souvent après la ménopause.",
                "Saignements vaginaux anormaux, pertes inhabituelles, douleurs pelviennes.");
        add("Cancer de l'estomac", "Carcinome", "Estomac", "estomac",
                "Adénocarcinome gastrique souvent lié à Helicobacter pylori.",
                "Douleurs épigastriques, nausées, vomissements, perte d'appétit, amaigrissement.");
        add("Cancer du foie", "Carcinome", "Foie", "foie",
                "Carcinome hépatocellulaire souvent associé à cirrhose ou hépatite chronique.",
                "Douleur droite, jaunisse, perte de poids, fatigue, abdomen enflé.");
        add("Cancer du pancréas", "Carcinome", "Pancréas", "pancreas",
                "Adénocarcinome pancréatique de mauvais pronostic, difficile à détecter tôt.",
                "Jaunisse, douleurs abdominales irradiant dans le dos, perte de poids.");
        add("Cancer de la thyroïde", "Carcinome", "Thyroïde", "thyroide",
                "Tumeur maligne de la glande thyroïde, souvent de bon pronostic.",
                "Nodule dans le cou, modification de la voix, difficultés à avaler.");
        add("Cancer du rein", "Carcinome", "Rein", "rein",
                "Carcinome à cellules rénales claires, souvent découvert fortuitement.",
                "Sang dans les urines, douleur dans le flanc, masse abdominale, fatigue.");
        add("Cancer de la vessie", "Carcinome", "Vessie", "pelvis",
                "Tumeur urothéliale de la paroi vésicale, fortement associée au tabagisme.",
                "Sang dans les urines, mictions douloureuses, envie fréquente d'uriner.");
        add("Cancer du rectum", "Carcinome", "Rectum", "colon",
                "Tumeur maligne de la partie terminale du gros intestin.",
                "Sang dans les selles, modification du transit, douleurs rectales.");
        add("Cancer de l'intestin grêle", "Carcinome", "Intestin grêle", "colon",
                "Tumeur rare de l'intestin grêle, adénocarcinome le plus fréquent.",
                "Douleurs abdominales, nausées, vomissements, occlusion intestinale.");
        add("Cancer de l'œsophage", "Carcinome", "Œsophage", "gorge",
                "Tumeur maligne de l'œsophage, liée au tabac, alcool ou reflux gastrique.",
                "Difficultés à avaler, perte de poids, douleurs thoraciques, régurgitations.");
        add("Cancer du larynx", "Carcinome", "Larynx", "gorge",
                "Tumeur maligne du larynx, fortement associée au tabagisme et à l'alcool.",
                "Enrouement persistant, douleurs à la gorge, difficultés à avaler.");
        add("Cancer de la langue", "Carcinome", "Langue", "tete",
                "Carcinome épidermoïde de la langue, lié au tabac, alcool ou HPV.",
                "Ulcération persistante, douleur linguale, difficulté à parler ou avaler.");
        add("Cancer de la bouche", "Carcinome", "Cavité buccale", "tete",
                "Tumeur maligne de la cavité buccale incluant lèvres, gencives, palais.",
                "Plaie qui ne guérit pas, taches blanches ou rouges, douleurs buccales.");
        add("Cancer du nasopharynx", "Carcinome", "Nasopharynx", "tete",
                "Tumeur maligne du nasopharynx, souvent associée au virus Epstein-Barr.",
                "Ganglions cervicaux, obstruction nasale, épistaxis, acouphènes.");
        add("Cancer de l'oropharynx", "Carcinome", "Oropharynx", "tete",
                "Cancer de la gorge moyenne, lié au HPV et au tabac.",
                "Mal de gorge persistant, difficultés à avaler, otalgie.");
        add("Cancer de l'hypopharynx", "Carcinome", "Hypopharynx", "tete",
                "Cancer rare de la partie inférieure du pharynx, mauvais pronostic.",
                "Douleur à la déglutition, dysphagie, voix étouffée.");
        add("Cancer des glandes salivaires", "Carcinome", "Glandes salivaires", "tete",
                "Tumeur rare des glandes salivaires, souvent de bas grade.",
                "Masse indolore dans la joue ou sous la mâchoire, douleur faciale.");
        add("Cancer du testicule", "Carcinome", "Testicule", "pelvis",
                "Tumeur germinale testiculaire, le plus fréquent chez l'homme jeune.",
                "Masse ou gonflement testiculaire indolore, sensation de lourdeur.");
        add("Cancer de la verge", "Carcinome", "Verge", "pelvis",
                "Tumeur maligne rare de la verge, associée au phimosis et HPV.",
                "Lésion ou ulcération sur le gland, écoulement, douleur.");
        add("Cancer de la peau (non mélanome)", "Carcinome", "Peau", "peau",
                "Carcinome basocellulaire ou épidermoïde cutané, lié aux UV.",
                "Lésion cutanée qui ne guérit pas, croûte, ulcération, plaque nacrée.");
        add("Carcinome basocellulaire", "Carcinome", "Peau", "peau",
                "Cancer cutané le plus fréquent, métastase rare, croissance lente.",
                "Petite boule nacrée, télangiectasies, ulcération centrale.");
        add("Carcinome épidermoïde cutané", "Carcinome", "Peau", "peau",
                "Deuxième cancer cutané le plus fréquent, risque métastatique.",
                "Lésion squameuse, croûte, ulcération, souvent sur zone exposée.");
        add("Cancer de la vésicule biliaire", "Carcinome", "Vésicule biliaire", "foie",
                "Adénocarcinome rare de la vésicule biliaire, souvent découvert tardivement.",
                "Douleurs abdominales droites, jaunisse, nausées, perte de poids.");
        add("Cancer des voies biliaires", "Carcinome", "Voies biliaires", "foie",
                "Cholangiocarcinome des canaux biliaires, de mauvais pronostic.",
                "Jaunisse, démangeaisons, selles décolorées, urines foncées.");
        add("Cancer de l'uretère", "Carcinome", "Uretère", "pelvis",
                "Tumeur urothéliale de l'uretère, rare et souvent agressive.",
                "Hématurie, douleur lombaire, infections urinaires.");
        add("Cancer de l'urètre", "Carcinome", "Urètre", "pelvis",
                "Tumeur rare de l'urètre, plus fréquente chez la femme.",
                "Saignements, mictions douloureuses, masse palpée.");
        add("Cancer du pénis", "Carcinome", "Pénis", "pelvis",
                "Tumeur maligne rare du pénis, associée au phimosis et HPV.",
                "Lésion, nodule ou ulcération sur le gland ou le prépuce.");
        add("Cancer du vagin", "Carcinome", "Vagin", "pelvis",
                "Tumeur maligne rare du vagin, souvent liée au HPV.",
                "Saignements post-ménopausiques, douleurs, leucorrhée.");
        add("Cancer de la vulve", "Carcinome", "Vulve", "pelvis",
                "Tumeur maligne rare de la vulve, fréquente après 65 ans.",
                "Prurit, douleur, lésion cutanée, saignement, masse.");
        add("Cancer de l'endomètre", "Carcinome", "Endomètre", "pelvis",
                "Cancer de la muqueuse utérine, le plus fréquent des cancers gynécologiques.",
                "Saignements post-ménopausiques, douleurs pelviennes.");
        add("Cancer de la trompe de Fallope", "Carcinome", "Trompe de Fallope", "pelvis",
                "Tumeur maligne rare des trompes utérines.",
                "Douleurs abdominales, leucorrhée, masse pelvienne.");
        add("Cancer du péritoine", "Carcinome", "Péritoine", "colon",
                "Carcinose péritonéale primitive ou secondaire.",
                "Ascite, douleurs abdominales, occlusion intestinale.");
        add("Cancer de l'œil (rétinoblastome)", "Carcinome", "Œil", "tete",
                "Tumeur maligne de la rétine, survient chez l'enfant.",
                "Leucocorie (reflet blanc dans la pupille), strabisme.");
        add("Cancer de l'orbite", "Carcinome", "Orbite", "tete",
                "Tumeur maligne de l'orbite, rare.",
                "Exophtalmie, douleur oculaire, baisse de vision.");
        add("Cancer des paupières", "Carcinome", "Paupières", "tete",
                "Carcinome basocellulaire ou épidermoïde des paupières.",
                "Lésion palpébrale, ulcération, perte de cils.");
        add("Cancer de l'oreille", "Carcinome", "Oreille", "tete",
                "Carcinome cutané du pavillon ou du conduit auditif.",
                "Lésion ulcérée, douleur, otorrhée.");
        add("Cancer de l'os temporal", "Carcinome", "Os temporal", "tete",
                "Tumeur maligne rare de l'os temporal.",
                "Otalgie, otorrhée, paralysie faciale, surdité.");

        // ═══════════════════════════════════════════════════════════
        //  LYMPHOMES
        // ═══════════════════════════════════════════════════════════
        add("Lymphome de Hodgkin", "Lymphome", "Ganglions lymphatiques", "ganglions",
                "Cancer du système lymphatique avec cellules de Reed-Sternberg, bon pronostic.",
                "Ganglions indolores, fièvre, sueurs nocturnes, perte de poids, fatigue.");
        add("Lymphome non hodgkinien", "Lymphome", "Ganglions lymphatiques", "ganglions",
                "Groupe hétérogène de cancers lymphatiques sans cellules de Reed-Sternberg.",
                "Ganglions enflés, fièvre, sueurs nocturnes, fatigue, perte de poids.");
        add("Lymphome diffus grandes cellules B", "Lymphome", "Ganglions lymphatiques", "ganglions",
                "Forme agressive de lymphome non hodgkinien, la plus fréquente chez l'adulte.",
                "Masse à croissance rapide, fièvre, sueurs nocturnes, fatigue intense.");
        add("Lymphome folliculaire", "Lymphome", "Ganglions lymphatiques", "ganglions",
                "Lymphome indolent à évolution lente, souvent incurable mais contrôlable.",
                "Ganglions indolores, fatigue légère, parfois asymptomatique.");
        add("Lymphome de Burkitt", "Lymphome", "Ganglions lymphatiques", "ganglions",
                "Lymphome très agressif à croissance rapide, associé au virus Epstein-Barr.",
                "Masse abdominale volumineuse, mâchoire enflée, fièvre, sueurs nocturnes.");
        add("Lymphome T cutané", "Lymphome", "Peau", "peau",
                "Mycosis fongoïde, forme cutanée de lymphome T à évolution lente.",
                "Plaques cutanées prurigineuses, érythrodermie, tumeurs cutanées.");
        add("Lymphome du manteau", "Lymphome", "Ganglions", "ganglions",
                "Lymphome B agressif, souvent diagnostiqué à un stade avancé.",
                "Adénopathies généralisées, atteinte digestive, fièvre.");
        add("Lymphome de la zone marginale", "Lymphome", "Ganglions", "ganglions",
                "Lymphome B indolent, souvent associé à une infection chronique.",
                "Adénopathies localisées, fatigue, parfois asymptomatique.");
        add("Lymphome T périphérique", "Lymphome", "Ganglions", "ganglions",
                "Groupe hétérogène de lymphomes T agressifs.",
                "Adénopathies généralisées, éruptions cutanées, fièvre.");
        add("Lymphome anaplasique à grandes cellules", "Lymphome", "Ganglions", "ganglions",
                "Lymphome T agressif, exprimant la protéine ALK.",
                "Adénopathies, masses cutanées, symptômes B (fièvre, sueurs).");
        add("Lymphome lymphoblastique", "Lymphome", "Ganglions, sang", "ganglions",
                "Lymphome agressif touchant les précurseurs lymphocytaires.",
                "Adénopathies, douleurs osseuses, insuffisance médullaire.");
        add("Maladie de Castleman", "Lymphome", "Ganglions", "ganglions",
                "Maladie lymphoproliférative, forme localisée ou multicentrique.",
                "Adénopathies, fièvre, fatigue, perte de poids.");

        // ═══════════════════════════════════════════════════════════
        //  LEUCÉMIES
        // ═══════════════════════════════════════════════════════════
        add("Leucémie aiguë lymphoblastique", "Leucémie", "Moelle osseuse", "moelle",
                "Cancer hématologique agressif touchant les lymphoblastes, fréquent chez l'enfant.",
                "Fatigue intense, pâleur, fièvre, infections répétées, saignements, douleurs osseuses.");
        add("Leucémie aiguë myéloblastique", "Leucémie", "Moelle osseuse", "moelle",
                "Cancer hématologique agressif touchant les cellules myéloïdes.",
                "Fatigue, pâleur, fièvre, saignements, ecchymoses, infections fréquentes.");
        add("Leucémie chronique lymphocytaire", "Leucémie", "Sang, moelle", "moelle",
                "Cancer d'évolution lente touchant les lymphocytes B matures.",
                "Souvent asymptomatique, ganglions, fatigue, infections répétées.");
        add("Leucémie chronique myéloïde", "Leucémie", "Moelle osseuse", "moelle",
                "Cancer lié au chromosome Philadelphie, prolifération des cellules myéloïdes.",
                "Fatigue, splénomégalie, douleurs abdominales, sudations nocturnes.");
        add("Leucémie à tricholeucocytes", "Leucémie", "Moelle, rate", "moelle",
                "Leucémie rare touchant les lymphocytes B, évolution lente.",
                "Fatigue, splénomégalie, infections récurrentes.");
        add("Leucémie prolymphocytaire", "Leucémie", "Sang, moelle", "moelle",
                "Leucémie agressive touchant les lymphocytes B ou T.",
                "Splénomégalie massive, adénopathies, cytopénies.");
        add("Leucémie à grands lymphocytes granuleux", "Leucémie", "Sang, moelle", "moelle",
                "Leucémie rare touchant les lymphocytes T ou NK.",
                "Neutropénie, infections, anémie, splénomégalie.");

        // ═══════════════════════════════════════════════════════════
        //  MYÉLOMES
        // ═══════════════════════════════════════════════════════════
        add("Myélome multiple", "Myélome", "Moelle osseuse", "moelle",
                "Cancer des plasmocytes produisant des immunoglobulines anormales.",
                "Douleurs osseuses, fractures pathologiques, fatigue, infections répétées.");
        add("Myélome solitaire", "Myélome", "Os", "os",
                "Prolifération plasmocytaire localisée à un seul site osseux.",
                "Douleur osseuse localisée, fracture pathologique, compression neurologique.");
        add("Plasmocytome", "Myélome", "Moelle osseuse", "moelle",
                "Tumeur de plasmocytes malins, peut évoluer vers un myélome multiple.",
                "Masse localisée, douleurs osseuses, symptômes neurologiques.");
        add("Maladie de Waldenström", "Myélome", "Moelle osseuse", "moelle",
                "Lymphome lymphoplasmocytaire avec production d'IgM monoclonale.",
                "Fatigue, anémie, troubles visuels, saignements, neuropathie.");
        add("Myélome multiple à chaînes légères", "Myélome", "Moelle osseuse", "moelle",
                "Variante du myélome multiple ne sécrétant que des chaînes légères.",
                "Insuffisance rénale, douleurs osseuses, anémie.");
        add("Myélome non sécrétant", "Myélome", "Moelle osseuse", "moelle",
                "Myélome sans production d'immunoglobuline détectable.",
                "Douleurs osseuses, fractures, anémie, sans pic monoclonal.");

        // ═══════════════════════════════════════════════════════════
        //  SARCOMES (tissus mous et os)
        // ═══════════════════════════════════════════════════════════
        add("Sarcome des os", "Sarcome", "Os", "os",
                "Tumeur maligne primitive des os, rare mais grave.",
                "Douleur osseuse persistante, gonflement, fracture pathologique.");
        add("Ostéosarcome", "Sarcome", "Os", "os",
                "Tumeur maligne osseuse la plus fréquente chez les adolescents.",
                "Douleur osseuse, gonflement, rougeur, fracture pathologique.");
        add("Sarcome d'Ewing", "Sarcome", "Os", "os",
                "Tumeur osseuse agressive liée à la translocation t(11;22).",
                "Douleur osseuse intense, gonflement, fièvre, fatigue.");
        add("Chondrosarcome", "Sarcome", "Cartilage", "os",
                "Tumeur maligne du cartilage, touchant les adultes de 40 à 70 ans.",
                "Douleur osseuse progressive, masse, gonflement, limitation des mouvements.");
        add("Sarcome d'Adamantinome", "Sarcome", "Os", "os",
                "Tumeur osseuse rare du tibia, de bas grade.",
                "Douleur tibiale, gonflement lente, parfois asymptomatique.");
        add("Sarcome myxoïde des os", "Sarcome", "Os", "os",
                "Tumeur osseuse rare produisant une matrice myxoïde.",
                "Douleur, gonflement, fracture pathologique.");
        add("Rhabdomyosarcome", "Sarcome", "Muscles", "muscles",
                "Tumeur maligne des cellules musculaires striées, fréquente chez l'enfant.",
                "Masse indolore, gonflement, symptômes selon localisation.");
        add("Liposarcome", "Sarcome", "Tissu adipeux", "muscles",
                "Tumeur maligne du tissu adipeux, souvent dans les membres.",
                "Masse volumineuse indolore, douleurs selon localisation.");
        add("Leiomyosarcome", "Sarcome", "Muscles lisses", "muscles",
                "Tumeur maligne des muscles lisses, souvent utérine ou digestive.",
                "Douleurs abdominales, saignements, masse palpable.");
        add("Sarcome de Kaposi", "Sarcome", "Peau / Organes", "peau",
                "Tumeur vasculaire maligne associée à HHV-8, fréquente chez les immunodéprimés.",
                "Lésions cutanées violacées, œdèmes, atteinte muqueuse.");
        add("Fibrosarcome", "Sarcome", "Tissu fibreux", "muscles",
                "Tumeur maligne des fibroblastes, rare mais agressive.",
                "Masse ferme et indolore, croissance lente puis rapide.");
        add("Sarcome synovial", "Sarcome", "Tissu mou", "muscles",
                "Tumeur maligne des parties molles près des articulations.",
                "Masse profonde et douloureuse, parfois calcifiée.");
        add("Sarcome pléomorphe indifférencié", "Sarcome", "Tissu mou", "muscles",
                "Sarcome agressif des parties molles, de haut grade.",
                "Masse à croissance rapide, douleur, limitation fonctionnelle.");
        add("Sarcome alvéolaire des parties molles", "Sarcome", "Tissu mou", "muscles",
                "Sarcome rare touchant l'adulte jeune.",
                "Masse indolore, métastases pulmonaires et cérébrales précoces.");
        add("Sarcome à cellules claires", "Sarcome", "Tissu mou", "muscles",
                "Sarcome rare touchant les tendons et aponévroses.",
                "Masse indolore, métastases ganglionnaires fréquentes.");
        add("Sarcome d'Ewing extra-osseux", "Sarcome", "Tissu mou", "muscles",
                "Forme extra-osseuse du sarcome d'Ewing.",
                "Masse des parties molles, douleur, fièvre.");
        add("Sarcome granulocytaire", "Sarcome", "Tissus divers", "moelle",
                "Tumeur extramédullaire de cellules myéloblastiques.",
                "Masse verte (chlorome), associée à la leucémie.");
        add("Angiosarcome", "Sarcome", "Vaisseaux sanguins, peau", "peau",
                "Tumeur maligne des vaisseaux sanguins.",
                "Lésion cutanée violacée, œdème, saignement.");
        add("Hémangioendothéliome", "Sarcome", "Vaisseaux", "peau",
                "Tumeur vasculaire de malignité intermédiaire.",
                "Masse cutanée ou viscérale, parfois multifocale.");
        add("Péricytome", "Sarcome", "Péricytes", "muscles",
                "Tumeur rare des péricytes, souvent bénigne mais peut être maligne.",
                "Masse indolore, parfois douloureuse.");
        add("Sarcome à cellules fusiformes", "Sarcome", "Tissu mou", "muscles",
                "Groupe hétérogène de sarcomes à cellules fusiformes.",
                "Masse indolore, croissance variable.");

        // ═══════════════════════════════════════════════════════════
        //  MÉLANOMES
        // ═══════════════════════════════════════════════════════════
        add("Mélanome cutané", "Mélanome", "Peau", "peau",
                "Cancer malin des mélanocytes cutanés, lié à l'exposition aux UV.",
                "Grain de beauté asymétrique, bords irréguliers, couleur hétérogène, diamètre > 6mm.");
        add("Mélanome superficiel extensif", "Mélanome", "Peau", "peau",
                "Sous-type le plus fréquent de mélanome, croissance horizontale initiale.",
                "Tache pigmentée irrégulière, évolution lente.");
        add("Mélanome acrolentigineux", "Mélanome", "Peau (paumes, plantes)", "peau",
                "Mélanome survenant sur les paumes, plantes ou zones unguéales.",
                "Tache pigmentée sur paume ou plante, lésion unguéale.");
        add("Mélanome nodulaire", "Mélanome", "Peau", "peau",
                "Forme agressive de mélanome, croissance verticale rapide.",
                "Nodule pigmenté ou non, saignant, à croissance rapide.");
        add("Mélanome lentigo malin", "Mélanome", "Peau (visage)", "peau",
                "Mélanome survenant sur les zones photo-exposées chez la personne âgée.",
                "Grande tache pigmentée du visage, évolution lente.");
        add("Mélanome uvéal", "Mélanome", "Œil", "tete",
                "Cancer malin de l'uvée (iris, corps ciliaire, choroïde).",
                "Troubles visuels, taches dans le champ visuel, décollement de rétine.");
        add("Mélanome conjonctival", "Mélanome", "Conjonctive", "tete",
                "Mélanome rare de la conjonctive oculaire.",
                "Tache pigmentée sur l'œil, lésion conjonctivale.");
        add("Mélanome muqueux", "Mélanome", "Muqueuses", "peau",
                "Mélanome rare des muqueuses (nasale, buccale, génitale), de mauvais pronostic.",
                "Lésion pigmentée sur les muqueuses, saignements, obstruction nasale.");
        add("Mélanome des parties molles", "Mélanome", "Tissus mous", "muscles",
                "Mélanome rare des tissus mous profonds (clear cell sarcoma).",
                "Masse indolore, souvent au niveau des membres.");
        add("Mélanome méningé", "Mélanome", "Méninges", "cerveau",
                "Mélanome primitif des leptoméninges, très rare et agressif.",
                "Céphalées, crises d'épilepsie, déficits neurologiques.");

        // ═══════════════════════════════════════════════════════════
        //  GLIOMES ET TUMEURS CÉRÉBRALES
        // ═══════════════════════════════════════════════════════════
        add("Gliome cérébral", "Gliome", "Cerveau", "cerveau",
                "Tumeur maligne issue des cellules gliales du cerveau.",
                "Céphalées, crises d'épilepsie, déficits neurologiques, troubles de la mémoire.");
        add("Glioblastome", "Gliome", "Cerveau", "cerveau",
                "Forme la plus agressive des gliomes, grade IV, survie < 15 mois.",
                "Céphalées intenses, crises d'épilepsie, déficits moteurs, troubles cognitifs.");
        add("Astrocytome", "Gliome", "Cerveau", "cerveau",
                "Tumeur dérivant des astrocytes, de pronostic variable selon le grade.",
                "Céphalées, crises d'épilepsie, troubles visuels, déficits neurologiques.");
        add("Astrocytome anaplasique", "Gliome", "Cerveau", "cerveau",
                "Astrocytome de grade III, agressif.",
                "Céphalées, crises, signes de focalisation.");
        add("Oligodendrogliome", "Gliome", "Cerveau", "cerveau",
                "Tumeur gliale à cellules d'oligodendrocytes, pronostic meilleur.",
                "Crises d'épilepsie, céphalées, déficits selon localisation.");
        add("Oligodendrogliome anaplasique", "Gliome", "Cerveau", "cerveau",
                "Forme agressive de l'oligodendrogliome.",
                "Crises, céphalées intenses, signes neurologiques.");
        add("Épendymome", "Gliome", "Cerveau, moelle épinière", "cerveau",
                "Tumeur gliale issue des épendymocytes, souvent proche des ventricules.",
                "Céphalées, nausées, vomissements, troubles de la marche.");
        add("Méningiome", "Tumeur méningée", "Méninges", "cerveau",
                "Tumeur des méninges souvent bénigne, fréquente chez la femme.",
                "Céphalées, crises d'épilepsie, troubles visuels, déficits moteurs.");
        add("Méningiome atypique", "Tumeur méningée", "Méninges", "cerveau",
                "Méningiome de grade II, récidives plus fréquentes.",
                "Signes neurologiques progressifs, céphalées.");
        add("Méningiome malin", "Tumeur méningée", "Méninges", "cerveau",
                "Méningiome de grade III, agressif, invasion locale.",
                "Symptômes neurologiques sévères, évolution rapide.");
        add("Médulloblastome", "Tumeur embryonnaire", "Cervelet", "cerveau",
                "Tumeur maligne cérébrale pédiatrique la plus fréquente.",
                "Céphalées matinales, vomissements, troubles de l'équilibre, démarche instable.");
        add("Neurome acoustique (schwannome vestibulaire)", "Tumeur nerveuse", "Nerf vestibulaire", "cerveau",
                "Tumeur bénigne de la gaine du nerf vestibulaire.",
                "Hypoacousie, acouphènes, troubles de l'équilibre.");
        add("Schwannome malin", "Tumeur nerveuse", "Nerfs périphériques", "muscles",
                "Tumeur maligne des cellules de Schwann, rare.",
                "Masse douloureuse, déficit neurologique.");
        add("Neuroblastome", "Tumeur embryonnaire", "Système nerveux sympathique", "cerveau",
                "Cancer pédiatrique du système nerveux sympathique.",
                "Masse abdominale, douleurs osseuses, fièvre, pâleur, exophtalmie.");
        add("Rétinoblastome", "Tumeur embryonnaire", "Rétine", "tete",
                "Tumeur maligne de la rétine chez l'enfant.",
                "Leucocorie (reflet blanc dans l'œil), strabisme.");
        add("Craniopharyngiome", "Tumeur épithéliale", "Région sellaire", "cerveau",
                "Tumeur bénigne de la région hypophysaire, souvent kystique.",
                "Céphalées, troubles visuels, insuffisance hypophysaire.");
        add("Hémangioblastome", "Tumeur vasculaire", "Cervelet", "cerveau",
                "Tumeur vasculaire bénigne du cervelet, parfois associée à la maladie de von Hippel-Lindau.",
                "Céphalées, troubles de l'équilibre, nausées.");
        add("Lymphome cérébral primitif", "Lymphome", "Cerveau", "cerveau",
                "Lymphome non hodgkinien primitif du système nerveux central.",
                "Déficits neurologiques, céphalées, confusion, crises.");
        add("Tumeur tératologique", "Tumeur germinale", "Gonades, médiastin, cerveau", "pelvis",
                "Tumeur des cellules germinales, bénigne ou maligne.",
                "Masse, symptômes selon localisation, douleur.");
        add("Germinome", "Tumeur germinale", "Système nerveux central", "cerveau",
                "Tumeur germinale du cerveau, très radiosensible.",
                "Diabète insipide, troubles visuels, déficits hormonaux.");

        // ═══════════════════════════════════════════════════════════
        //  TUMEURS ENDOCRINIENNES
        // ═══════════════════════════════════════════════════════════
        add("Tumeur neuroendocrine", "Tumeur endocrine", "Système neuroendocrine", "pancreas",
                "Tumeur des cellules neuroendocrines, de malignité variable.",
                "Douleurs abdominales, diarrhée flush, syndrome carcinodien.");
        add("Tumeur carcinoïde", "Tumeur endocrine", "Tube digestif, poumon", "colon",
                "Tumeur neuroendocrine bien différenciée, évolution lente.",
                "Flush, diarrhée, douleurs abdominales, bronchospasme.");
        add("Phéochromocytome", "Tumeur endocrine", "Glande surrénale", "rein",
                "Tumeur sécrétant des catécholamines, souvent bénigne.",
                "Hypertension paroxystique, céphalées, palpitations, sueurs.");
        add("Paragangliome", "Tumeur endocrine", "Ganglions sympathiques", "ganglions",
                "Tumeur des paraganglions, sécrétant des catécholamines.",
                "Hypertension, palpitations, céphalées (si sécrétant).");
        add("Insulinome", "Tumeur endocrine", "Pancréas", "pancreas",
                "Tumeur des cellules β pancréatiques sécrétant de l'insuline.",
                "Hypoglycémies (sueurs, palpitations, confusion, perte de connaissance).");
        add("Gastrinome", "Tumeur endocrine", "Pancréas, duodénum", "pancreas",
                "Tumeur sécrétant de la gastrine, responsable du syndrome de Zollinger-Ellison.",
                "Ulcères gastriques multiples, diarrhée, douleurs abdominales.");
        add("Glucagonome", "Tumeur endocrine", "Pancréas", "pancreas",
                "Tumeur sécrétant du glucagon.",
                "Érythème nécrolytique migrateur, diabète, perte de poids.");
        add("VIPome", "Tumeur endocrine", "Pancréas", "pancreas",
                "Tumeur sécrétant le peptide intestinal vasoactif (VIP).",
                "Diarrhée aqueuse profuse, hypokaliémie, déshydratation.");
        add("Somatostatinome", "Tumeur endocrine", "Pancréas, duodénum", "pancreas",
                "Tumeur sécrétant la somatostatine.",
                "Diabète, lithiase biliaire, diarrhée, malabsorption.");
        add("Tumeur thyroïdienne médullaire", "Tumeur endocrine", "Thyroïde", "thyroide",
                "Cancer de la thyroïde issu des cellules C (calcitonine).",
                "Nodule thyroïdien, diarrhée, flush (si métastases).");
        add("Carcinome surrénalien", "Tumeur endocrine", "Glande surrénale", "rein",
                "Cancer rare de la corticosurrénale, agressif.",
                "Syndrome de virilisation ou de Cushing, masse abdominale.");
        add("Tumeur hypophysaire", "Tumeur endocrine", "Hypophyse", "cerveau",
                "Tumeur bénigne ou maligne de l'hypophyse.",
                "Troubles visuels, céphalées, syndrome endocrinien (acromégalie, etc.).");
        add("Prolactinome", "Tumeur endocrine", "Hypophyse", "cerveau",
                "Tumeur hypophysaire sécrétant la prolactine.",
                "Galactorrhée, troubles menstruels, impuissance.");
        add("Corticotrophinome", "Tumeur endocrine", "Hypophyse", "cerveau",
                "Tumeur sécrétant l'ACTH.",
                "Syndrome de Cushing (obésité centrale, vergetures, HTA).");
        add("Somatotrophinome", "Tumeur endocrine", "Hypophyse", "cerveau",
                "Tumeur sécrétant la GH.",
                "Acromégalie (élargissement des extrémités, prognathisme).");
        add("Thyréotrophinome", "Tumeur endocrine", "Hypophyse", "cerveau",
                "Tumeur sécrétant la TSH.",
                "Hypertension, palpitations, nervosité.");

        // ═══════════════════════════════════════════════════════════
        //  TUMEURS GYNÉCOLOGIQUES SPÉCIFIQUES
        // ═══════════════════════════════════════════════════════════
        add("Tumeur des cellules de la granulosa", "Tumeur gynécologique", "Ovaire", "pelvis",
                "Tumeur ovarienne sécrétant des œstrogènes.",
                "Métrorragies (post-ménopausiques), puberté précoce.");
        add("Dysmorphome (dysgerminome)", "Tumeur gynécologique", "Ovaire", "pelvis",
                "Tumeur germinale maligne de l'ovaire.",
                "Masse pelvienne, douleurs, métrorragies.");
        add("Tératome ovarien", "Tumeur gynécologique", "Ovaire", "pelvis",
                "Tumeur germinale de l'ovaire, souvent bénigne.",
                "Masse pelvienne, douleurs, parfois syndrome de encéphalite.");
        add("Choriocarcinome", "Tumeur gynécologique", "Utérus", "pelvis",
                "Tumeur maligne du trophoblaste, post-grossesse.",
                "Métrorragies, élévation des HCG, métastases pulmonaires.");
        add("Sarcome utérin", "Sarcome", "Utérus", "pelvis",
                "Sarcome du muscle utérin ou du stroma endométrial.",
                "Saignements, douleurs pelviennes, masse abdominale.");
        add("LEIOMYOSARCOME utérin", "Sarcome", "Utérus", "pelvis",
                "Sarcome le plus fréquent de l'utérus.",
                "Saignements, douleurs, masse pelvienne rapidement progressive.");

        // ═══════════════════════════════════════════════════════════
        //  TUMEURS PÉDIATRIQUES
        // ═══════════════════════════════════════════════════════════
        add("Tumeur de Wilms (néphroblastome)", "Tumeur pédiatrique", "Rein", "rein",
                "Tumeur rénale maligne de l'enfant.",
                "Masse abdominale, hématurie, HTA, fièvre.");
        add("Hépatoblastome", "Tumeur pédiatrique", "Foie", "foie",
                "Tumeur hépatique maligne du jeune enfant.",
                "Masse abdominale, vomissements, perte de poids.");
        add("Neuroblastome", "Tumeur pédiatrique", "Système sympathique", "cerveau",
                "Tumeur sympathique de l'enfant.",
                "Masse abdominale, ecchymoses, exophtalmie.");
        add("Rhabdomyosarcome", "Sarcome", "Muscles", "muscles",
                "Sarcome pédiatrique des muscles striés.",
                "Masse selon localisation (orbite, cavité nasale, vessie).");
        add("Tumeur d'Askin", "Sarcome", "Thorax", "poumon",
                "Sarcome d'Ewing du thorax.",
                "Masse thoracique, douleur, dyspnée.");
        add("Tumeur germinale du nouveau-né", "Tumeur pédiatrique", "Gonades", "pelvis",
                "Tumeur germinale congénitale.",
                "Masse abdominale ou pelvienne, hydrocéphalie si cérébrale.");

        // ═══════════════════════════════════════════════════════════
        //  TUMEURS RARES
        // ═══════════════════════════════════════════════════════════
        add("Tumeur desmoïde", "Tumeur rare", "Tissu conjonctif", "muscles",
                "Tumeur fibreuse agressive localement, sans métastases.",
                "Masse indolore, douleur, limitation fonctionnelle.");
        add("Mésothéliome", "Tumeur rare", "Plèvre, péritoine", "poumon",
                "Tumeur de la séreuse liée à l'amiante.",
                "Dyspnée, douleur thoracique, épanchement pleural.");
        add("Chordome", "Tumeur rare", "Colonne vertébrale", "os",
                "Tumeur du notochorde, localisation sacro-coccygienne.",
                "Douleurs sacro-coccygiennes, troubles sphinctériens.");
        add("Myxome cardiaque", "Tumeur rare", "Cœur", "coeur",
                "Tumeur bénigne du cœur, la plus fréquente des tumeurs cardiaques.",
                "Emboles, dyspnée, fièvre, douleurs.");
        add("Tumeur d'Abrikossof", "Tumeur rare", "Peau, muqueuse", "peau",
                "Tumeur des cellules granuleuses, bénigne.",
                "Nodule indolore, souvent lingual.");
        add("Histiocytose langerhansienne", "Tumeur rare", "Os, peau, poumon", "os",
                "Prolifération de cellules de Langerhans, bénigne ou maligne.",
                "Lésions osseuses lytiques, polyurie, éruption cutanée.");
        add("Tumeur de Pancoast", "Carcinome", "Sommet pulmonaire", "poumon",
                "Cancer du poumon du sommet, envahissant le plexus brachial.",
                "Douleur épaule-bras (syndrome de Pancoast-Tobias).");
        add("Tumeur carcinoïde thymique", "Tumeur endocrine", "Thymus", "gorge",
                "Tumeur neuroendocrine du thymus.",
                "Syndrome de Cushing, myasthénie (si associée).");
        add("Tumeur neuroectodermique primitive", "Sarcome", "Os, tissus mous", "os",
                "Tumeur rare proche du sarcome d'Ewing.",
                "Masse douloureuse, fièvre, signes généraux.");
    }

    private static void add(String n, String c, String o, String z, String d, String s) {
        ALL_CANCERS.add(new CancerEntry(n, c, o, z, d, s));
    }

    // ═══════════════════════════════════════════════════════════════
    //  MÉTHODES PUBLIQUES
    // ═══════════════════════════════════════════════════════════════
    public void setOnSave(Consumer<Cancer> cb) { this.onSaveCallback = cb; }

    public void setAddMode() {
        isEditMode = false; editId = 0;
        if (lblTitle != null) lblTitle.setText("➕ Ajouter un cancer");
        resetForm();
    }

    public void setEditMode(Cancer c) {
        isEditMode = true; editId = c.getId();
        if (lblTitle != null) lblTitle.setText("✏️ Modifier — " + c.getNom());
        autoFilling = true;
        tfNom.setText(c.getNom());
        cbClassification.setValue(c.getClassification());
        cbStade.setValue(c.getStade());
        tfOrgane.setText(c.getOrgane());
        taDescription.setText(c.getDescription());
        taSymptomes.setText(c.getSymptomes());
        autoFilling = false;
        updateValidateBtn();
        highlightZoneForOrgane(c.getOrgane());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbClassification.setItems(FXCollections.observableArrayList(
                "Carcinome", "Sarcome", "Lymphome", "Leucémie", "Mélanome",
                "Myélome", "Gliome", "Tumeur endocrine", "Tumeur gynécologique",
                "Tumeur pédiatrique", "Tumeur rare", "Autre"));
        cbStade.setItems(FXCollections.observableArrayList("0","I","II","III","IV"));

        drawHumanBody3D();

        // Trier les cancers par ordre alphabétique
        ALL_CANCERS.sort((a, b) -> a.nom.compareToIgnoreCase(b.nom));
        refreshList(ALL_CANCERS);

        listCancers.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null)
                ALL_CANCERS.stream().filter(c -> c.nom.equals(val)).findFirst()
                        .ifPresent(this::applySelection);
        });

        tfNom.textProperty().addListener((o, ov, nv) -> updateValidateBtn());
        cbClassification.valueProperty().addListener((o, ov, nv) -> updateValidateBtn());
        cbStade.valueProperty().addListener((o, ov, nv) -> updateValidateBtn());
        tfOrgane.textProperty().addListener((o, ov, nv) -> updateValidateBtn());
    }

    @FXML private void onSearch() {
        String q = tfSearch.getText().trim().toLowerCase();
        refreshList(q.isEmpty() ? ALL_CANCERS :
                ALL_CANCERS.stream().filter(c ->
                        c.nom.toLowerCase().contains(q) ||
                                c.cls.toLowerCase().contains(q) ||
                                c.organe.toLowerCase().contains(q) ||
                                c.desc.toLowerCase().contains(q)
                ).collect(Collectors.toList()));
    }

    private void refreshList(List<CancerEntry> entries) {
        listCancers.setItems(FXCollections.observableArrayList(
                entries.stream().map(c -> c.nom).collect(Collectors.toList())));

        // Afficher le nombre de résultats dans un tooltip
        int count = entries.size();
        listCancers.setTooltip(new Tooltip(count + " cancer(s) trouvé(s)"));
    }

    private void applySelection(CancerEntry e) {
        autoFilling = true;
        tfNom.setText(e.nom);
        cbClassification.setValue(e.cls);
        tfOrgane.setText(e.organe);
        taDescription.setText(e.desc);
        taSymptomes.setText(e.symp);
        String teal = "-fx-border-color: #2BBCB0; -fx-border-width: 2px; -fx-border-radius: 6px;";
        cbClassification.setStyle(teal); tfOrgane.setStyle(teal);
        taDescription.setStyle(teal); taSymptomes.setStyle(teal);
        if (lblSelectedZone != null) lblSelectedZone.setText("📍 " + e.organe);
        autoFilling = false;
        highlightZone(e.zone);
        updateValidateBtn();
    }

    private void applySelectionByZone(String zoneId) {
        ALL_CANCERS.stream().filter(c -> c.zone.equals(zoneId)).findFirst().ifPresent(entry -> {
            applySelection(entry);
            int idx = listCancers.getItems().indexOf(entry.nom);
            if (idx >= 0) {
                listCancers.getSelectionModel().select(idx);
                listCancers.scrollTo(idx);
            }
        });
    }

    private void highlightZoneForOrgane(String organe) {
        ALL_CANCERS.stream().filter(c -> c.organe.equalsIgnoreCase(organe))
                .findFirst().ifPresent(e -> highlightZone(e.zone));
    }

    @FXML private void onEnregistrer() {
        if (onSaveCallback == null) return;
        Cancer c = new Cancer();
        c.setId(editId);
        c.setNom(tfNom.getText().trim());
        c.setClassification(cbClassification.getValue());
        c.setStade(cbStade.getValue());
        c.setOrgane(tfOrgane.getText().trim());
        c.setDescription(taDescription.getText().trim());
        c.setSymptomes(taSymptomes.getText().trim());
        onSaveCallback.accept(c);
        closeStage();
    }

    @FXML private void onAnnuler() { closeStage(); }
    private void closeStage() { ((Stage) btnEnregistrer.getScene().getWindow()).close(); }

    private void updateValidateBtn() {
        btnEnregistrer.setDisable(
                tfNom.getText().trim().isEmpty() ||
                        cbClassification.getValue() == null ||
                        cbStade.getValue() == null ||
                        tfOrgane.getText().trim().isEmpty()
        );
    }

    private void resetForm() {
        autoFilling = true;
        tfNom.clear(); cbClassification.setValue(null); cbStade.setValue(null);
        tfOrgane.clear(); taDescription.clear(); taSymptomes.clear();
        cbClassification.setStyle(""); tfOrgane.setStyle("");
        taDescription.setStyle(""); taSymptomes.setStyle("");
        if (lblSelectedZone != null) lblSelectedZone.setText("");
        autoFilling = false;
        updateValidateBtn();
        highlightedZone = null;
        zoneShapes.forEach((id, shapes) -> shapes.forEach(s -> {
            s.setOpacity(0.75);
            s.setEffect(null);
        }));
    }

    private void highlightZone(String zoneId) {
        highlightedZone = zoneId;
        DropShadow glow = new DropShadow(20, Color.web("#2BBCB0", 0.9));
        glow.setSpread(0.4);
        zoneShapes.forEach((id, shapes) -> {
            if (id.equals(zoneId)) {
                shapes.forEach(s -> {
                    s.setOpacity(1.0);
                    s.setEffect(glow);
                    s.toFront();
                });
            } else {
                shapes.forEach(s -> {
                    s.setOpacity(0.45);
                    s.setEffect(null);
                });
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  DESSIN DU CORPS HUMAIN 3D AVEC OMBRES ET PROFONDEUR
    // ═══════════════════════════════════════════════════════════════

    private void drawHumanBody3D() {
        // Fond avec dégradé médical
        bodyPane.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #E8F4F8, #D0E8F0);");

        double cx = 160; // Centre X

        // Couleurs avec tons chair réalistes
        zoneColors.put("tete",      new String[]{"#E8B89D", "#C49473", "#FDBCA0"});
        zoneColors.put("gorge",     new String[]{"#E0AE92", "#BC8A6A", "#F4B094"});
        zoneColors.put("cerveau",   new String[]{"#C8DFF0", "#90B8D8", "#A0CCE8"});
        zoneColors.put("thyroide",  new String[]{"#A0E8D0", "#68C8B0", "#88E0C8"});
        zoneColors.put("poumon",    new String[]{"#B0E8D8", "#78D0C0", "#98E0D0"});
        zoneColors.put("sein",      new String[]{"#F0C8D8", "#D890A8", "#E8B8C8"});
        zoneColors.put("coeur",     new String[]{"#F0A8A8", "#D86060", "#E89090"});
        zoneColors.put("estomac",   new String[]{"#FAD085", "#E0B050", "#F8E0A0"});
        zoneColors.put("foie",      new String[]{"#F4B060", "#D88830", "#F8C878"});
        zoneColors.put("pancreas",  new String[]{"#FAD8A0", "#E0B870", "#F8E8B0"});
        zoneColors.put("rein",      new String[]{"#C8B0E8", "#A088D0", "#D0C0F0"});
        zoneColors.put("colon",     new String[]{"#C8E8A0", "#98C860", "#D8F0B0"});
        zoneColors.put("pelvis",    new String[]{"#D8D0F0", "#B8A8E0", "#E8E0F8"});
        zoneColors.put("os",        new String[]{"#E8E0D0", "#D0C8B8", "#F0E8E0"});
        zoneColors.put("muscles",   new String[]{"#F0D0B8", "#D8A080", "#F8E0C8"});
        zoneColors.put("peau",      new String[]{"#F5CCB8", "#E0A088", "#FDE0D0"});
        zoneColors.put("ganglions", new String[]{"#E0C8F0", "#B898D8", "#F0E0F8"});
        zoneColors.put("moelle",    new String[]{"#F8C8C8", "#E89090", "#FCE0E0"});

        // Silhouette 3D avec ombre portée
        drawSilhouette3D(cx);

        // Dessiner tous les organes avec effet 3D
        drawHead3D(cx);
        drawNeck3D(cx);
        drawBrain3D(cx);
        drawThyroid3D(cx);
        drawLungs3D(cx);
        drawBreasts3D(cx);
        drawHeart3D(cx);
        drawStomach3D(cx);
        drawLiver3D(cx);
        drawPancreas3D(cx);
        drawKidneys3D(cx);
        drawColon3D(cx);
        drawPelvis3D(cx);
        drawLymphNodes3D(cx);
        drawSpine3D(cx);
        drawBones3D(cx);
        drawArms3D(cx);
        drawSkin3D(cx);
    }

    private void drawSilhouette3D(double cx) {
        // Ombre portée du corps
        Path shadow = new Path();
        shadow.getElements().addAll(
                new MoveTo(cx - 48, 115),
                new CubicCurveTo(cx - 70, 125, cx - 78, 145, cx - 75, 170),
                new CubicCurveTo(cx - 72, 205, cx - 65, 240, cx - 62, 275),
                new CubicCurveTo(cx - 60, 305, cx - 58, 330, cx - 60, 365),
                new LineTo(cx + 60, 365),
                new CubicCurveTo(cx + 58, 330, cx + 60, 305, cx + 62, 275),
                new CubicCurveTo(cx + 65, 240, cx + 72, 205, cx + 75, 170),
                new CubicCurveTo(cx + 78, 145, cx + 70, 125, cx + 48, 115),
                new ClosePath()
        );
        shadow.setFill(Color.web("#000000", 0.15));
        shadow.setTranslateX(4);
        shadow.setTranslateY(6);
        shadow.setMouseTransparent(true);
        bodyPane.getChildren().add(shadow);

        // Corps principal
        Path body = new Path();
        body.getElements().addAll(
                new MoveTo(cx - 45, 113),
                new CubicCurveTo(cx - 65, 118, cx - 72, 135, cx - 68, 160),
                new CubicCurveTo(cx - 64, 195, cx - 58, 230, cx - 55, 265),
                new CubicCurveTo(cx - 53, 295, cx - 51, 320, cx - 53, 355),
                new LineTo(cx - 33, 355),
                new LineTo(cx - 31, 510),
                new LineTo(cx + 31, 510),
                new LineTo(cx + 33, 355),
                new LineTo(cx + 53, 355),
                new CubicCurveTo(cx + 51, 320, cx + 53, 295, cx + 55, 265),
                new CubicCurveTo(cx + 58, 230, cx + 64, 195, cx + 68, 160),
                new CubicCurveTo(cx + 72, 135, cx + 65, 118, cx + 45, 113),
                new ClosePath()
        );

        LinearGradient skinGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#F5E6D8")),
                new Stop(0.5, Color.web("#FDEFE5")),
                new Stop(1, Color.web("#F0DCC8")));
        body.setFill(skinGrad);
        body.setStroke(Color.web("#D4A882", 0.7));
        body.setStrokeWidth(1.5);
        body.setMouseTransparent(true);

        InnerShadow innerShadow = new InnerShadow(5, Color.web("#C4A482", 0.3));
        body.setEffect(innerShadow);
        bodyPane.getChildren().add(body);
    }

    private void drawHead3D(double cx) {
        Ellipse headShadow = new Ellipse(cx + 3, 56, 37, 46);
        headShadow.setFill(Color.web("#000000", 0.12));
        headShadow.setMouseTransparent(true);
        bodyPane.getChildren().add(headShadow);

        Ellipse head = new Ellipse(cx, 52, 34, 42);
        RadialGradient headGrad = new RadialGradient(0, 0, 0.4, 0.4, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FDBCA0")),
                new Stop(0.5, Color.web("#E8B89D")),
                new Stop(1, Color.web("#C49473")));
        head.setFill(headGrad);
        head.setStroke(Color.web("#B88464", 0.8));
        head.setStrokeWidth(1.2);
        addZone3D("tete", List.of(head), "Tête", cx - 12, 18);

        // Traits du visage
        drawFaceFeatures(cx);
    }

    private void drawFaceFeatures(double cx) {
        Ellipse leftEye = new Ellipse(cx - 12, 42, 4, 3);
        leftEye.setFill(Color.web("#4A3528"));
        Ellipse rightEye = new Ellipse(cx + 12, 42, 4, 3);
        rightEye.setFill(Color.web("#4A3528"));

        Circle leftReflect = new Circle(cx - 13, 41, 1, Color.web("#FFFFFF", 0.6));
        Circle rightReflect = new Circle(cx + 11, 41, 1, Color.web("#FFFFFF", 0.6));

        Path nose = new Path();
        nose.getElements().addAll(
                new MoveTo(cx, 45),
                new CubicCurveTo(cx - 2, 50, cx - 1, 54, cx, 56),
                new CubicCurveTo(cx + 1, 54, cx + 2, 50, cx, 45));
        nose.setFill(Color.web("#C49473", 0.5));

        Path mouth = new Path();
        mouth.getElements().addAll(
                new MoveTo(cx - 8, 62),
                new CubicCurveTo(cx - 5, 64, cx + 5, 64, cx + 8, 62));
        mouth.setStroke(Color.web("#B07050", 0.7));
        mouth.setStrokeWidth(1.5);
        mouth.setFill(null);

        leftEye.setMouseTransparent(true);
        rightEye.setMouseTransparent(true);
        leftReflect.setMouseTransparent(true);
        rightReflect.setMouseTransparent(true);
        nose.setMouseTransparent(true);
        mouth.setMouseTransparent(true);

        bodyPane.getChildren().addAll(leftEye, rightEye, leftReflect, rightReflect, nose, mouth);
    }

    private void drawNeck3D(double cx) {
        Rectangle neck = new Rectangle(cx - 13, 91, 26, 22);
        neck.setArcWidth(8);
        neck.setArcHeight(8);
        LinearGradient neckGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#E0AE92")),
                new Stop(0.5, Color.web("#F4B094")),
                new Stop(1, Color.web("#BC8A6A")));
        neck.setFill(neckGrad);
        neck.setStroke(Color.web("#B88464", 0.8));
        neck.setStrokeWidth(1);
        addZone3D("gorge", List.of(neck), "Gorge", cx + 14, 105);
    }

    private void drawBrain3D(double cx) {
        Ellipse brain = new Ellipse(cx, 48, 26, 30);
        RadialGradient brainGrad = new RadialGradient(0, 0, 0.4, 0.4, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#A0CCE8")),
                new Stop(0.6, Color.web("#C8DFF0")),
                new Stop(1, Color.web("#90B8D8")));
        brain.setFill(brainGrad);
        brain.setStroke(Color.web("#7898B8", 0.8));
        brain.setStrokeWidth(1);
        addZone3D("cerveau", List.of(brain), "Cerveau", cx - 10, 22);
    }

    private void drawThyroid3D(double cx) {
        Ellipse thyroid = new Ellipse(cx, 108, 18, 10);
        RadialGradient thyGrad = new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#88E0C8")),
                new Stop(0.5, Color.web("#A0E8D0")),
                new Stop(1, Color.web("#68C8B0")));
        thyroid.setFill(thyGrad);
        thyroid.setStroke(Color.web("#58A898", 0.8));
        addZone3D("thyroide", List.of(thyroid), "Thyroïde", cx - 8, 106);
    }

    private void drawLungs3D(double cx) {
        Path leftLung = new Path();
        leftLung.getElements().addAll(
                new MoveTo(cx - 28, 135),
                new CubicCurveTo(cx - 48, 138, cx - 52, 165, cx - 45, 185),
                new CubicCurveTo(cx - 38, 200, cx - 22, 198, cx - 18, 185),
                new CubicCurveTo(cx - 14, 170, cx - 12, 142, cx - 28, 135));

        Path rightLung = new Path();
        rightLung.getElements().addAll(
                new MoveTo(cx + 28, 135),
                new CubicCurveTo(cx + 48, 138, cx + 52, 165, cx + 45, 185),
                new CubicCurveTo(cx + 38, 200, cx + 22, 198, cx + 18, 185),
                new CubicCurveTo(cx + 14, 170, cx + 12, 142, cx + 28, 135));

        RadialGradient lungGrad = new RadialGradient(0, 0, 0.4, 0.4, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#98E0D0")),
                new Stop(0.5, Color.web("#B0E8D8")),
                new Stop(1, Color.web("#78D0C0")));
        leftLung.setFill(lungGrad);
        rightLung.setFill(lungGrad);
        leftLung.setStroke(Color.web("#68B8A8", 0.8));
        rightLung.setStroke(Color.web("#68B8A8", 0.8));

        addZone3D("poumon", List.of(leftLung, rightLung), "Poumons", cx + 30, 140);
    }

    private void drawBreasts3D(double cx) {
        Ellipse leftBreast = new Ellipse(cx - 22, 178, 18, 15);
        RadialGradient breastGrad = new RadialGradient(0, 0, 0.4, 0.4, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#E8B8C8")),
                new Stop(0.5, Color.web("#F0C8D8")),
                new Stop(1, Color.web("#D890A8")));
        leftBreast.setFill(breastGrad);
        leftBreast.setStroke(Color.web("#C88098", 0.8));

        Ellipse rightBreast = new Ellipse(cx + 22, 178, 18, 15);
        rightBreast.setFill(breastGrad);
        rightBreast.setStroke(Color.web("#C88098", 0.8));

        addZone3D("sein", List.of(leftBreast, rightBreast), "Sein", cx + 28, 170);
    }

    private void drawHeart3D(double cx) {
        Path heart = new Path();
        heart.getElements().addAll(
                new MoveTo(cx - 8, 155),
                new CubicCurveTo(cx - 22, 148, cx - 26, 162, cx - 14, 170),
                new CubicCurveTo(cx - 6, 176, cx, 168, cx, 164),
                new CubicCurveTo(cx, 168, cx + 6, 176, cx + 14, 170),
                new CubicCurveTo(cx + 26, 162, cx + 22, 148, cx + 8, 155),
                new ClosePath());

        RadialGradient heartGrad = new RadialGradient(0, 0, 0.4, 0.4, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#E89090")),
                new Stop(0.5, Color.web("#F0A8A8")),
                new Stop(1, Color.web("#D86060")));
        heart.setFill(heartGrad);
        heart.setStroke(Color.web("#C85050", 0.9));
        heart.setStrokeWidth(1.2);

        Ellipse heartReflect = new Ellipse(cx - 4, 154, 6, 4);
        heartReflect.setFill(Color.web("#FFFFFF", 0.3));
        heartReflect.setMouseTransparent(true);
        bodyPane.getChildren().add(heartReflect);

        addZone3D("coeur", List.of(heart), "Cœur", cx - 32, 148);
    }

    private void drawStomach3D(double cx) {
        Ellipse stomach = new Ellipse(cx - 10, 218, 20, 16);
        RadialGradient stomachGrad = new RadialGradient(0, 0, 0.4, 0.4, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#F8E0A0")),
                new Stop(0.5, Color.web("#FAD085")),
                new Stop(1, Color.web("#E0B050")));
        stomach.setFill(stomachGrad);
        stomach.setStroke(Color.web("#D0A040", 0.8));
        addZone3D("estomac", List.of(stomach), "Estomac", cx - 42, 215);
    }

    private void drawLiver3D(double cx) {
        Path liver = new Path();
        liver.getElements().addAll(
                new MoveTo(cx + 8, 204),
                new CubicCurveTo(cx + 30, 200, cx + 38, 212, cx + 32, 224),
                new CubicCurveTo(cx + 24, 234, cx + 6, 232, cx + 2, 224),
                new CubicCurveTo(cx - 2, 216, cx + 2, 206, cx + 8, 204));

        RadialGradient liverGrad = new RadialGradient(0, 0, 0.4, 0.4, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#F8C878")),
                new Stop(0.5, Color.web("#F4B060")),
                new Stop(1, Color.web("#D88830")));
        liver.setFill(liverGrad);
        liver.setStroke(Color.web("#C87828", 0.8));
        addZone3D("foie", List.of(liver), "Foie", cx + 24, 205);
    }

    private void drawPancreas3D(double cx) {
        Ellipse pancreas = new Ellipse(cx, 240, 28, 9);
        RadialGradient pancGrad = new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#F8E8B0")),
                new Stop(0.5, Color.web("#FAD8A0")),
                new Stop(1, Color.web("#E0B870")));
        pancreas.setFill(pancGrad);
        pancreas.setStroke(Color.web("#D0A860", 0.8));
        addZone3D("pancreas", List.of(pancreas), "Pancréas", cx - 10, 236);
    }

    private void drawKidneys3D(double cx) {
        Path leftKidney = new Path();
        leftKidney.getElements().addAll(
                new MoveTo(cx - 40, 250),
                new CubicCurveTo(cx - 52, 248, cx - 54, 264, cx - 42, 266),
                new CubicCurveTo(cx - 34, 268, cx - 28, 264, cx - 32, 254),
                new CubicCurveTo(cx - 34, 250, cx - 36, 250, cx - 40, 250));

        Path rightKidney = new Path();
        rightKidney.getElements().addAll(
                new MoveTo(cx + 40, 250),
                new CubicCurveTo(cx + 52, 248, cx + 54, 264, cx + 42, 266),
                new CubicCurveTo(cx + 34, 268, cx + 28, 264, cx + 32, 254),
                new CubicCurveTo(cx + 34, 250, cx + 36, 250, cx + 40, 250));

        RadialGradient kidneyGrad = new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#D0C0F0")),
                new Stop(0.5, Color.web("#C8B0E8")),
                new Stop(1, Color.web("#A088D0")));
        leftKidney.setFill(kidneyGrad);
        rightKidney.setFill(kidneyGrad);
        leftKidney.setStroke(Color.web("#9078C0", 0.8));
        rightKidney.setStroke(Color.web("#9078C0", 0.8));

        addZone3D("rein", List.of(leftKidney, rightKidney), "Reins", cx + 42, 248);
    }

    private void drawColon3D(double cx) {
        Path colon = new Path();
        colon.getElements().addAll(
                new MoveTo(cx - 38, 272),
                new LineTo(cx + 38, 272),
                new CubicCurveTo(cx + 54, 274, cx + 54, 308, cx + 38, 310),
                new LineTo(cx - 38, 310),
                new CubicCurveTo(cx - 54, 308, cx - 54, 274, cx - 38, 272));

        RadialGradient colonGrad = new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#D8F0B0")),
                new Stop(0.5, Color.web("#C8E8A0")),
                new Stop(1, Color.web("#98C860")));
        colon.setFill(colonGrad);
        colon.setStroke(Color.web("#88B850", 0.8));
        addZone3D("colon", List.of(colon), "Côlon", cx - 50, 285);
    }

    private void drawPelvis3D(double cx) {
        Ellipse pelvis = new Ellipse(cx, 330, 38, 22);
        RadialGradient pelvisGrad = new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#E8E0F8")),
                new Stop(0.5, Color.web("#D8D0F0")),
                new Stop(1, Color.web("#B8A8E0")));
        pelvis.setFill(pelvisGrad);
        pelvis.setStroke(Color.web("#A898D0", 0.8));
        addZone3D("pelvis", List.of(pelvis), "Pelvis", cx - 14, 328);
    }

    private void drawLymphNodes3D(double cx) {
        List<Shape> lymph = new ArrayList<>();
        double[][] positions = {
                {cx - 50, 128, 8}, {cx + 50, 128, 8},
                {cx - 48, 200, 7}, {cx + 48, 200, 7},
                {cx - 45, 310, 7}, {cx + 45, 310, 7}
        };

        for (double[] pos : positions) {
            Circle node = new Circle(pos[0], pos[1], pos[2]);
            RadialGradient nodeGrad = new RadialGradient(0, 0, 0.4, 0.4, 0.6, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#F0E0F8")),
                    new Stop(0.5, Color.web("#E0C8F0")),
                    new Stop(1, Color.web("#B898D8")));
            node.setFill(nodeGrad);
            node.setStroke(Color.web("#A888C8", 0.8));
            lymph.add(node);
        }

        addZone3D("ganglions", lymph, "Ganglions", cx + 52, 122);
    }

    private void drawSpine3D(double cx) {
        Rectangle spine = new Rectangle(cx - 5, 120, 10, 200);
        spine.setArcWidth(6);
        spine.setArcHeight(6);
        LinearGradient spineGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#E89090")),
                new Stop(0.5, Color.web("#F8C8C8")),
                new Stop(1, Color.web("#E89090")));
        spine.setFill(spineGrad);
        spine.setStroke(Color.web("#D87878", 0.8));
        addZone3D("moelle", List.of(spine), "Moelle", cx + 6, 190);
    }

    private void drawBones3D(double cx) {
        Path leftFemur = new Path();
        leftFemur.getElements().addAll(
                new MoveTo(cx - 18, 358),
                new CubicCurveTo(cx - 28, 370, cx - 28, 385, cx - 18, 395),
                new LineTo(cx - 10, 395),
                new CubicCurveTo(cx - 14, 385, cx - 14, 370, cx - 10, 358),
                new ClosePath());

        Path rightFemur = new Path();
        rightFemur.getElements().addAll(
                new MoveTo(cx + 18, 358),
                new CubicCurveTo(cx + 28, 370, cx + 28, 385, cx + 18, 395),
                new LineTo(cx + 10, 395),
                new CubicCurveTo(cx + 14, 385, cx + 14, 370, cx + 10, 358),
                new ClosePath());

        LinearGradient boneGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#D0C8B8")),
                new Stop(0.5, Color.web("#E8E0D0")),
                new Stop(1, Color.web("#D0C8B8")));
        leftFemur.setFill(boneGrad);
        rightFemur.setFill(boneGrad);
        leftFemur.setStroke(Color.web("#C0B8A8", 0.8));
        rightFemur.setStroke(Color.web("#C0B8A8", 0.8));

        addZone3D("os", List.of(leftFemur, rightFemur), "Os", cx + 38, 380);
    }

    private void drawArms3D(double cx) {
        Path leftArm = new Path();
        leftArm.getElements().addAll(
                new MoveTo(cx - 58, 125),
                new CubicCurveTo(cx - 78, 135, cx - 82, 185, cx - 74, 265),
                new LineTo(cx - 62, 265),
                new CubicCurveTo(cx - 66, 185, cx - 64, 135, cx - 58, 125));

        Path rightArm = new Path();
        rightArm.getElements().addAll(
                new MoveTo(cx + 58, 125),
                new CubicCurveTo(cx + 78, 135, cx + 82, 185, cx + 74, 265),
                new LineTo(cx + 62, 265),
                new CubicCurveTo(cx + 66, 185, cx + 64, 135, cx + 58, 125));

        LinearGradient armGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#D8A080")),
                new Stop(0.5, Color.web("#F0D0B8")),
                new Stop(1, Color.web("#D8A080")));
        leftArm.setFill(armGrad);
        rightArm.setFill(armGrad);
        leftArm.setStroke(Color.web("#C89070", 0.8));
        rightArm.setStroke(Color.web("#C89070", 0.8));

        addZone3D("muscles", List.of(leftArm, rightArm), "Muscles", cx + 68, 200);
    }

    private void drawSkin3D(double cx) {
        Rectangle leftSkin = new Rectangle(cx - 92, 115, 16, 170);
        leftSkin.setArcWidth(12);
        leftSkin.setArcHeight(12);
        Rectangle rightSkin = new Rectangle(cx + 76, 115, 16, 170);
        rightSkin.setArcWidth(12);
        rightSkin.setArcHeight(12);

        LinearGradient skinGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#E0A088")),
                new Stop(0.5, Color.web("#F5CCB8")),
                new Stop(1, Color.web("#E0A088")));
        leftSkin.setFill(skinGrad);
        rightSkin.setFill(skinGrad);
        leftSkin.setStroke(Color.web("#D09070", 0.8));
        rightSkin.setStroke(Color.web("#D09070", 0.8));

        addZone3D("peau", List.of(leftSkin, rightSkin), "Peau", cx - 90, 190);
    }

    private void addZone3D(String zoneId, List<Shape> shapes, String label, double lx, double ly) {
        String[] colors = zoneColors.getOrDefault(zoneId, new String[]{"#CCCCCC","#888888","#EEEEEE"});

        for (Shape s : shapes) {
            s.setCursor(Cursor.HAND);
            s.setOpacity(0.75);

            InnerShadow inner3D = new InnerShadow(3, Color.web(colors[1], 0.3));
            s.setEffect(inner3D);

            s.setOnMouseEntered(e -> {
                if (!zoneId.equals(highlightedZone)) {
                    s.setOpacity(1.0);
                    DropShadow hover = new DropShadow(12, Color.web(colors[1], 0.7));
                    hover.setSpread(0.3);
                    s.setEffect(hover);
                }
            });

            s.setOnMouseExited(e -> {
                if (!zoneId.equals(highlightedZone)) {
                    s.setOpacity(0.75);
                    s.setEffect(inner3D);
                }
            });

            s.setOnMouseClicked(e -> applySelectionByZone(zoneId));
            bodyPane.getChildren().add(s);
        }

        Text txt = new Text(lx, ly, label);
        txt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        txt.setFill(Color.web(colors[1]).darker());
        txt.setMouseTransparent(true);
        txt.setEffect(new DropShadow(2, Color.web("#FFFFFF", 0.5)));
        bodyPane.getChildren().add(txt);

        zoneShapes.put(zoneId, shapes);
    }

    private RadialGradient createGradient(String color1, String color2) {
        return new RadialGradient(0, 0, 0.5, 0.5, 0.6, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(color1)),
                new Stop(1, Color.web(color2)));
    }

    public static class CancerEntry {
        public final String nom, cls, organe, zone, desc, symp;
        public CancerEntry(String n, String c, String o, String z, String d, String s) {
            nom = n; cls = c; organe = o; zone = z; desc = d; symp = s;
        }
    }
}