package projet_java;
import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
		

			public static void main(String[] args) {
				// TODO Auto-generated method stub
							    String url = "jdbc:sqlite:BDD_pj_javadb"; // Fichier de base de données local
							    Scanner sc = new Scanner(System.in);

						        try {
						            Connection conn = DriverManager.getConnection(url);
						            System.out.println("Connexion établie avec la base SQLite.");

						            // ── Afficher la liste de tous les ingrédients ──────────────────
						            int v1 = 3; 
						            int v2 = 0;
						            
						            String sql2 = "SELECT id_ingredient, nom FROM ingredients where id_ingredient <= ? and id_ingredient > ?";
						            
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
						                System.out.print("Combien d'aliments voulez-vous choisir ? (1 à 5) : ");
						                nb_aliment = sc.nextInt();
						                if (nb_aliment < 1 || nb_aliment > 5) {
						                    System.out.println("Veuillez entrer un nombre entre 1 et 5.");
						                }
						            } while (nb_aliment < 1 || nb_aliment > 5);
						            
						            
						            ArrayList<Integer> diff_ingre = new ArrayList<>();
						            
						            
						            String sql1 = "SELECT nom FROM ingredient WHERE id_ingredient = ?";

						            for (int x = 0; x < nb_aliment; x++) {
						                // for plus lisible que do/while ici : on sait exactement
						                // combien de tours on fait (nb_aliment fois)

						                System.out.print("Aliment " + (x + 1) + " - Entrez l'id : ");
						                int id_aliment = sc.nextInt();
						                diff_ingre.add(id_aliment);

						                PreparedStatement pst1 = conn.prepareStatement(sql1);
						                pst1.setInt(1, id_aliment);

						                try (ResultSet rs1 = pst1.executeQuery()) {
						                    if (rs1.next()) {
						                        // ✅ rs1 et non rs — c'est le résultat de la recherche par id
						                        System.out.println("  → " + rs1.getString("nom") + " ajouté !");
						                    } else {
						                        System.out.println("  → Aucun ingrédient trouvé avec cet id, réessayez.");
						                        x--; // on ne compte pas ce tour, l'utilisateur doit réessayer
						                    }
						                }
						            }
						            
						            System.out.println("\nVos " + nb_aliment + " aliment(s) ont été sélectionnés !");

						            conn.close();

						        } catch (SQLException e) {
						            System.err.println("Erreur SQL : " + e.getMessage());
						        }

						        sc.close();
						    }
						}