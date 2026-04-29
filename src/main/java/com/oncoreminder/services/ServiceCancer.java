package com.oncoreminder.services;

import com.oncoreminder.models.Cancer;
import com.oncoreminder.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCancer {

    private Connection cnx;

    public ServiceCancer() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // ─────────── CREATE ───────────
    public void ajouter(Cancer c) throws SQLException {
        String sql = "INSERT INTO cancer (nom, description, classification, stade, organe, symptomes) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        ps.setString(3, c.getClassification());
        ps.setString(4, c.getStade());
        ps.setString(5, c.getOrgane());
        ps.setString(6, c.getSymptomes());
        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            c.setId(keys.getInt(1));
        }
    }

    // ─────────── READ ALL ───────────
    public List<Cancer> afficherTous() throws SQLException {
        List<Cancer> list = new ArrayList<>();
        String sql = "SELECT * FROM cancer ORDER BY nom ASC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Cancer c = mapRow(rs);
            list.add(c);
        }
        return list;
    }

    // ─────────── READ BY ID ───────────
    public Cancer afficherParId(int id) throws SQLException {
        String sql = "SELECT * FROM cancer WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }

    // ─────────── SEARCH ───────────
    public List<Cancer> rechercher(String keyword) throws SQLException {
        List<Cancer> list = new ArrayList<>();
        String sql = "SELECT * FROM cancer WHERE nom LIKE ? OR classification LIKE ? OR organe LIKE ? ORDER BY nom ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        String k = "%" + keyword + "%";
        ps.setString(1, k);
        ps.setString(2, k);
        ps.setString(3, k);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    // ─────────── UPDATE ───────────
    public void modifier(Cancer c) throws SQLException {
        String sql = "UPDATE cancer SET nom=?, description=?, classification=?, stade=?, organe=?, symptomes=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        ps.setString(3, c.getClassification());
        ps.setString(4, c.getStade());
        ps.setString(5, c.getOrgane());
        ps.setString(6, c.getSymptomes());
        ps.setInt(7, c.getId());
        ps.executeUpdate();
    }

    // ─────────── DELETE ───────────
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM cancer WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ─────────── COUNT ───────────
    public int compter() throws SQLException {
        String sql = "SELECT COUNT(*) FROM cancer";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    // ─────────── HELPER ───────────
    private Cancer mapRow(ResultSet rs) throws SQLException {
        Cancer c = new Cancer();
        c.setId(rs.getInt("id"));
        c.setNom(rs.getString("nom"));
        c.setDescription(rs.getString("description"));
        c.setClassification(rs.getString("classification"));
        c.setStade(rs.getString("stade"));
        c.setOrgane(rs.getString("organe"));
        c.setSymptomes(rs.getString("symptomes"));
        return c;
    }
}
