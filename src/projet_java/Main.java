package projet_java;
import java.sql.*;

import java.util.Scanner;
import java.util.ArrayList;
public class Main {
		

			public static void main(String[] args) {
				// TODO Auto-generated method stub
							    String url = "jdbc:sqlite:BDD_pj_java.db.db"; // Fichier de base de données local
							    Scanner sc = new Scanner(System.in);

							    try {

						            //   CONNEXION À LA BASE DE DONNÉES

						            Connection conn = DriverManager.getConnection(url);

						            System.out.println("╔══════════════════════════════════════╗");
						            System.out.println("║      Bienvenue sur RecipEasy !       ║");
						            System.out.println("╚══════════════════════════════════════╝");

						            //   AFFICHAGE DE LA LISTE DES INGRÉDIENTS
						            //   (3 par ligne grâce aux variables v1/v2)
						            
						            System.out.println("\n Voici les ingrédients disponibles :\n");

						            int v1 = 3;
						            int v2 = 0;
						            String sql2 = "SELECT id_ingredient, nom FROM ingredient " +
						                          "WHERE id_ingredient <= ? AND id_ingredient > ?";

						            while (v1 <= 52) {

						                PreparedStatement pst2 = conn.prepareStatement(sql2);
						                pst2.setInt(1, v1);
						                pst2.setInt(2, v2);

						                try (ResultSet rs2 = pst2.executeQuery()) {
						                    while (rs2.next()) {
						                        System.out.print(rs2.getInt("id_ingredient") + " - "
						                            + rs2.getString("nom") + " | ");
						                    }
						                }

						                v1 += 3;
						                v2 += 3;
						                System.out.println();
						            }

						            //   SAISIE DU NOMBRE D'INGRÉDIENTS (1 à 5)

						            int nb_aliment = 0;

						            do {
						                System.out.print("\nCombien d'ingrédients souhaitez-vous utiliser ? (1 à 5) : ");

						                if (sc.hasNextInt()) {
						                    nb_aliment = sc.nextInt();
						                    if (nb_aliment < 1 || nb_aliment > 5) {
						                        System.out.println("⚠ Veuillez entrer un nombre entre 1 et 5.");
						                    }
						                } else {
						                    System.out.println("⚠ Saisie invalide, veuillez entrer un nombre entier.");
						                    sc.next(); // vider le token invalide
						                }

						            } while (nb_aliment < 1 || nb_aliment > 5);

						            //   SAISIE DES INGRÉDIENTS PAR L'UTILISATEUR
						            
						            ArrayList<Ingredient> diff_ingre = new ArrayList<>();
						            String sql1 = "SELECT nom FROM ingredient WHERE id_ingredient = ?";

						            for (int x = 0; x < nb_aliment; x++) {

						                System.out.print("\nIngrédient " + (x + 1) + "/" + nb_aliment + " — Entrez le numéro : ");

						                // vérification : l'utilisateur a tapé une lettre ?
						                if (!sc.hasNextInt()) {
						                    System.out.println("⚠ Ce n'est pas un numéro valide, réessayez.");
						                    sc.next(); // vider le token invalide
						                    x--;       // ne pas compter ce tour
						                    continue;
						                }

						                int id_aliment = sc.nextInt();

						                PreparedStatement pst1 = conn.prepareStatement(sql1);
						                pst1.setInt(1, id_aliment);

						                try (ResultSet rs1 = pst1.executeQuery()) {
						                    if (rs1.next()) {
						                    	Ingredient ingredient = new Ingredient(id_aliment ,rs1.getString("nom"));
						                        System.out.println("  ✔ " + ingredient.getNom() + " ajouté !");
						                        diff_ingre.add(ingredient); // on ajoute uniquement si l'id existe
						                    } else {
						                        System.out.println("  ✘ Aucun ingrédient trouvé avec ce numéro, réessayez.");
						                        x--; // ne pas compter ce tour
						                    }
						                }
						            }

						            System.out.println("\n✔ Vos " + nb_aliment + " ingrédient(s) ont été sélectionnés !");

						            //   RECHERCHE DES RECETTES CORRESPONDANTES

						            // seuil : au moins la moitié des ingrédients doit correspondre
						            // ex : 3 ingrédients -> seuil 2 | 5 ingrédients -> seuil 3
						            int seuilMinimum = (int) Math.ceil(nb_aliment / 3.0);

						            System.out.println("\n🔍 Recherche des recettes contenant au moins "
						                + seuilMinimum + "/" + nb_aliment + " de vos ingrédients...\n");

						            // construction du IN (?, ?, ...) avec autant de ? que d'ingrédients
						            StringBuilder placeholders = new StringBuilder();
						            for (int i = 0; i < nb_aliment; i++) {
						                placeholders.append("?");
						                if (i < nb_aliment - 1) placeholders.append(", ");
						            }

						            String sqlRecette =
						            	    "SELECT r.id_recette, r.nom, COUNT(*) AS nb_trouves, " +
						            	    // sous-requête qui compte le total d'ingrédients de chaque recette
						            	    "(SELECT COUNT(*) FROM recette_ingredient WHERE id_recette = r.id_recette) AS nb_total " +
						            	    "FROM recette r " +
						            	    "INNER JOIN recette_ingredient ri ON r.id_recette = ri.id_recette " +
						            	    "WHERE ri.id_ingredient IN (" + placeholders + ") " +
						            	    "GROUP BY r.id_recette, r.nom " +
						            	    "HAVING COUNT(*) >= ? " +
						            	    "ORDER BY nb_trouves DESC";

						            PreparedStatement pstRecette = conn.prepareStatement(sqlRecette);

						            // injection des ids des ingrédients choisis
						            for (int i = 0; i < nb_aliment; i++) {
						                pstRecette.setInt(i + 1, diff_ingre.get(i).getId());
						            }
						            // injection du seuil minimum (dernier ?)
						            pstRecette.setInt(nb_aliment + 1, seuilMinimum);

						            //   AFFICHAGE DES RECETTES TROUVÉES

						            ArrayList<Integer> idsRecettes = new ArrayList<>();
						            int v4 = 0;

						            try (ResultSet rsRecette = pstRecette.executeQuery()) {

						                System.out.println("╔══════════════════════════════╗");
						                System.out.println("║      Recettes suggérées      ║");
						                System.out.println("╚══════════════════════════════╝");

						                boolean trouve = false;

						                while (rsRecette.next()) {
						                    trouve = true;
						                    idsRecettes.add(rsRecette.getInt("id_recette"));

						                    int nb_trouves = rsRecette.getInt("nb_trouves");
						                    int nb_total   = rsRecette.getInt("nb_total");   // total d'ingrédients de la recette

						                    System.out.println(v4 + " - " +
						                        rsRecette.getString("nom") +
						                        "  (" + nb_trouves + "/" + nb_total + " ingrédients)");
						                    v4++;
						                }
						                
						                if (!trouve) {
						                    System.out.println("Aucune recette trouvée avec ces ingrédients.");
						                    System.out.println("Conseil : essayez avec des ingrédients différents.");
						                }
						            }

        					        //   SÉLECTION ET AFFICHAGE D'UNE RECETTE

						            String sql3 = "SELECT * FROM recette WHERE id_recette = ?";

						            while (true) {

						                System.out.print("\nEntrez le numéro d'une recette pour voir ses détails (100 pour quitter) : ");

						                // vérification : l'utilisateur a tapé une lettre ?
						                if (!sc.hasNextInt()) {
						                    System.out.println("⚠ Saisie invalide, entrez un numéro ou 100 pour quitter.");
						                    sc.next(); // vider le token invalide
						                    continue;
						                }

						                int id_select = sc.nextInt();

						                // quitter le programme
						                if (id_select == 100) {
						                    break;
						                }

						                // vérification : le numéro est dans la liste ?
						                if (id_select < 0 || id_select >= idsRecettes.size()) {
						                    System.out.println("⚠ Ce numéro ne correspond à aucune recette proposée.");
						                    System.out.println("   Choisissez un numéro entre 0 et " + (idsRecettes.size() - 1) + ".");
						                    continue;
						                }

						                // affichage du détail de la recette choisie
						                try (PreparedStatement pst3 = conn.prepareStatement(sql3)) {

						                    pst3.setInt(1, idsRecettes.get(id_select));

						                    try (ResultSet rs = pst3.executeQuery()) {
						                    	Recette recette = new Recette(id_select, rs.getString("nom"));
						                    	if (rs.next()) {
						                            System.out.println("\n╔══════════════════════════════════════╗");
						                            System.out.println("  " + recette.getNom());
						                            System.out.println("╚══════════════════════════════════════╝");
						                            System.out.println("📝 Description   : " + rs.getString("description"));
						                            System.out.println("⏱ Temps de prépa : " + rs.getString("temps_preparation") + " minutes");
						                            System.out.println("👨‍🍳 Instructions  : " + rs.getString("instructions"));
						                        }
						                    }
						                }
						            }

						            //   FIN DU PROGRAMME

						            System.out.println("\nMerci d'avoir utilisé RecipEasy, à bientôt !");
						            conn.close();

						        } catch (SQLException e) {
						            System.err.println("Erreur SQL : " + e.getMessage());
						        }

						        sc.close();
						    }
						}