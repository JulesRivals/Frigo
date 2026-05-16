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
						            Connection conn = DriverManager.getConnection(url);
						            System.out.println("Connexion établie avec la base SQLite.");

						            // ── Afficher la liste de tous les ingrédients ──────────────────
						            System.out.println("╔══════════════════════════════════════╗");
						            System.out.println("║      Bienvenue sur RecipEasy !       ║");
						            System.out.println("╚══════════════════════════════════════╝");
						            System.out.println("\n Voici les ingrédients disponibles :\n");
						            
						            int v1 = 3; 
						            int v2 = 0;
						            
						            String sql2 = "SELECT id_ingredient, nom FROM ingredient where id_ingredient <= ? and id_ingredient > ?";
						            
						            while(v1 <= 13) {
						                PreparedStatement pst2 = conn.prepareStatement(sql2);
					                    pst2.setInt(1, v1);
					                    pst2.setInt(2, v2);
					                
					                    try(ResultSet rs2 = pst2.executeQuery()){
					                	    while (rs2.next()) {
					                		    System.out.print(rs2.getInt("id_ingredient") + " - " + rs2.getString("nom") + " | ");
					                	    }
					                     }
					                    v1 += 3;
					                    v2 += 3;
					                    System.out.println();
						            }
					                
						            
						            // ── Demander combien d'aliments (entre 1 et 5) ─────────────────
						            int nb_aliment = 0;
						            do {
						            	 System.out.print("\nCombien d'ingrédients souhaitez-vous utiliser ? (1 à 5) : ");
						            	 if (sc.hasNextInt()) {
						                     // hasNextInt() vérifie que c'est bien un entier avant de lire
						                     nb_aliment = sc.nextInt();
						                     if (nb_aliment < 1 || nb_aliment > 5) {
						                         System.out.println(" Veuillez entrer un nombre entre 1 et 5.");
						                     }
						                 } else {
						                     // l'utilisateur a tapé une lettre ou autre chose
						                     System.out.println("⚠ Saisie invalide, veuillez entrer un nombre entier.");
						                     sc.next(); // vider le token invalide pour ne pas boucler infiniment
						                 }
						            } while (nb_aliment < 1 || nb_aliment > 5);
						            
						            
						            ArrayList<Integer> diff_ingre = new ArrayList<>();
						            
						            
						            String sql1 = "SELECT nom FROM ingredient where id_ingredient = ?";

						            for (int x = 0; x < nb_aliment; x++) {
						            	System.out.print("Ingrédient " + (x + 1) + "/" + nb_aliment + " — Entrez le numéro : ");

						                if (!sc.hasNextInt()) {
						                    // l'utilisateur a tapé une lettre
						                    System.out.println(" Ce n'est pas un numéro valide, réessayez.");
						                    sc.next(); // vider le token invalide
						                    x--;       // ne pas compter ce tour
						                    continue;  // retour au début du for
						                }
						                
						                int id_aliment = sc.nextInt(); 
						                diff_ingre.add(id_aliment);

						                PreparedStatement pst1 = conn.prepareStatement(sql1);
						                pst1.setInt(1, id_aliment);

						                try (ResultSet rs1 = pst1.executeQuery()) {
						                    if (rs1.next()) {
						                        // ✅ rs1 et non rs — c'est le résultat de la recherche par id
						                        System.out.println("  → " + rs1.getString("nom") + " ajouté !");
						                    } else {
						                        System.out.println("  → Aucun ingrédient trouvé avec ce numéro, réessayez.");
						                        x--; // on ne compte pas ce tour, l'utilisateur doit réessayer
						                    }
						                }
						            }
						            
						            System.out.println("\nVos " + nb_aliment + " aliment(s) ont été sélectionnés !");
						            
						            int seuilMinimum = (int) Math.ceil(nb_aliment / 2.0);
						            System.out.println("Recherche des recettes avec au moins "
						                + seuilMinimum + "/" + nb_aliment + " ingrédients...\n");

						            // ── Construire le IN (?, ?, ...) avec autant de ? que d'ingrédients ─
						            StringBuilder placeholders = new StringBuilder();
						            for (int i = 0; i < nb_aliment; i++) {
						                placeholders.append("?");
						                if (i < nb_aliment - 1) placeholders.append(", ");
						            }
						            // placeholders vaut "?" si 1 ingrédient, "?, ?" si 2, etc.
						            
						            ArrayList<Integer> idsRecettes = new ArrayList<>();


						            String sqlRecette =
						                "SELECT r.id_recette, r.nom, COUNT(*) AS nb_trouves " +
						                "FROM recette r " +
						                "INNER JOIN recette_ingredient ri ON r.id_recette = ri.id_recette " +
						                "WHERE ri.id_ingredient IN (" + placeholders + ") " +
						                "GROUP BY r.id_recette, r.nom " +
						                "HAVING COUNT(*) >= ? " +
						                "ORDER BY nb_trouves DESC";

						            PreparedStatement pstRecette = conn.prepareStatement(sqlRecette);

						            // Injecter les ids un par un dans les ?
						            for (int i = 0; i < nb_aliment; i++) {
						                pstRecette.setInt(i + 1, diff_ingre.get(i));
						            }
						            // Injecter le seuil dans le dernier ?
						            pstRecette.setInt(nb_aliment + 1, seuilMinimum);
						            
						            int v4 = 0;

						            // ── Afficher les recettes trouvées ────────────────────────────────
						            try (ResultSet rsRecette = pstRecette.executeQuery()) {
						            	System.out.println("╔══════════════════════════════╗");
						                System.out.println("║      Recettes suggérées      ║");
						                System.out.println("╚══════════════════════════════╝");
						                boolean trouve = false;
						                while (rsRecette.next()) {
						                    trouve = true;
						                    
						                    int idRecette = rsRecette.getInt("id_recette");   // ← récupérer l'id
						                    idsRecettes.add(idRecette);      
						                    
						                    System.out.println(v4 + " - " +
						                        rsRecette.getString("nom") +
						                        "  (" + rsRecette.getInt("nb_trouves") +
						                        "/" + nb_aliment + " ingrédients)"
						                    );
						                    v4 += 1;
						                }
						                if (!trouve) {
						                    System.out.println("Aucune recette trouvée avec ces ingrédients.");
						                }
						                
						                
						            }
				 
						           
						           while (true) {

						        	    // le message est TOUJOURS affiché en début de boucle
						        	   System.out.print("\nEntrez le numéro d'une recette pour voir ses détails (100 pour quitter) : ");

						        	    if (!sc.hasNextInt()) {
						        	        System.out.println("⚠ Saisie invalide, entrez un numéro ou 100 pour quitter.");
						        	        sc.next();
						        	        continue; // retour au début → réaffiche le message
						        	    }

						        	    int id_select = sc.nextInt();

						        	    if (id_select == 100) {
						        	        break; // fonctionne du premier coup maintenant
						        	    }

						        	    if (id_select < 0 || id_select >= idsRecettes.size()) {
						        	        System.out.println("⚠ Ce numéro ne correspond à aucune recette proposée.");
						        	        System.out.println("Choisissez un numéro entre 0 et " + (idsRecettes.size() - 1) + ".");
						        	        continue; // retour au début → réaffiche le message
						        	    }

						            
						             
						            	String sql3 = "SELECT * FROM recette WHERE id_recette = ?";

						            	try (PreparedStatement  pst1 = conn.prepareStatement(sql3)) {

						            	    pst1.setInt(1, idsRecettes.get(id_select));          

						            	    try (ResultSet rs = pst1.executeQuery()) {
						            	        if(rs.next()) {
						            	            System.out.println("\n==== " + rs.getString("nom") + " ====" + "\n ==> " + rs.getString("description") + 
						            	            		"\n Temps de préparation : " + rs.getString("temps_preparation") + " minutes." +
						            	            		 "\n Préparation : " + rs.getString("instructions")
						            	            		);
						            	        }
						            	    }
						            	}
						             
						            
						            
						            }
						           
						           System.out.println(" FIN ");
						            
						            conn.close();

						        } catch (SQLException e) {
						            System.err.println("Erreur SQL : " + e.getMessage());
						        }

						        sc.close();
						    }
						}