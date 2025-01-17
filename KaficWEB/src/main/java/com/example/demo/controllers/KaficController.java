package com.example.demo.controllers;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.NarudzbinaDTO;
import com.example.demo.DTO.PiceDTO;
import com.example.demo.DTO.ZaposleniDTO;
import com.example.demo.exceptions.MyException;
import com.example.demo.service.KaficSevice;

import jakarta.servlet.http.HttpServletResponse;
import model.Message;
import model.Pice;
import model.Zaposleni;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@RestController
@RequestMapping("api/cont/")
@CrossOrigin("*")
public class KaficController {

	@Autowired
	public KaficSevice service;
	
	/*////

	OVAJ DEO JE ZA KATEGORIJE
	
	*/////
	
	@GetMapping("getKategorije")
	public ResponseEntity<?> getKategorije(){
		return ResponseEntity.status(HttpStatus.OK).body(service.getKategorije());
	}
	
	@GetMapping("getKategorijaPoNazivu")
	public ResponseEntity<?> getKategorijaPoNazivu(@RequestParam String naziv){
		return ResponseEntity.status(HttpStatus.OK).body(service.getKategorija(naziv));
	}
	
	/*////

	OVAJ DEO JE ZA PICE
	
	*/////
	
	@GetMapping("savePiceMagacin")
	public ResponseEntity<Message> savePiceMagacin(@RequestParam int id, @RequestParam int kolicina){
		System.out.println(id);
		System.out.println(kolicina);
		Pice pice = service.savePiceMagacin(id, kolicina);
		if(pice == null) {
			return new ResponseEntity<Message>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<Message>(new Message("Uspesno ste promenili kolicinu."), HttpStatus.OK);
	}
	
	@GetMapping("getPicaFrontDTO")
	public ResponseEntity<?> getPicaFrontDTO(){
		return ResponseEntity.status(HttpStatus.OK).body(service.getPicaFrontDTO());
	}
	
	@GetMapping("getSvaPica")
	public ResponseEntity<?> getSvaPica(){
		return ResponseEntity.status(HttpStatus.OK).body(service.getSvaPica());
	}
	
	@GetMapping("getPicaPoKategoriji")
	public ResponseEntity<?> getPicaPoKategoriji(@RequestParam String kategorija){
		return ResponseEntity.status(HttpStatus.OK).body(service.getPicaPoKategoriji(kategorija));
	}
	
	@PostMapping("savePice")
	public ResponseEntity<?> savePice(@RequestBody PiceDTO p, @RequestParam int idk){
		if(p.getNaziv() == "" || p.getCena() <= 0) {
			throw new MyException("Svako pice mora imati naziv i adekvatnu cenu.");
		}
		if(service.getKategPoId(idk) == false) {
			throw new MyException("Data kategorija ne postoji. Morate je ili dodati ili koristiti postojece.");
		}
		Integer idNovogPica = service.savePice(p,idk);
		if(idNovogPica == -1) {
			throw new MyException("Greska prilikom dodavanja novog pica.");
		}
		return ResponseEntity.ok("Uspesno sacuvano novo pice, ciji je id: " +idNovogPica);
	}
	
	@GetMapping("izmeniCenu")
    public ResponseEntity<Void> izmeniCenu(@RequestParam String naziv, @RequestParam int novaCena) {
        service.izmeniCenuPica(naziv, novaCena);
        return ResponseEntity.ok().build();
    }
	
	/*////

	OVAJ DEO JE ZA ZAPOSLENOG
	
	*/////
	
	@GetMapping("getZaposlenogLogin")
	public ResponseEntity<?> getZaposlenogLogin(@RequestParam String us, @RequestParam String ps){
		Zaposleni zaposleni = service.getZaposleniLogin(us, ps);
		if(zaposleni == null) {
			return ResponseEntity.notFound().build();
		}
		ZaposleniDTO dto = new ZaposleniDTO();
		dto.setIme(zaposleni.getIme());
		dto.setPassword(zaposleni.getPassword());
		dto.setPrezime(zaposleni.getPrezime());
		dto.setUloga(zaposleni.getUloga());
		dto.setUsername(zaposleni.getUsername());
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
	
	@PostMapping("saveZaposlenog")
	public ResponseEntity<?> saveZaposlenog(@RequestBody ZaposleniDTO z){
		if(z.getIme() == "" || z.getPrezime() == "" || z.getUloga() == "") {
			throw new MyException("Svaki zaposleni mora da ima adekvatno ime, prezime i ulogu.");
		}
		Integer idNovogZapos = service.dodajZaposlenog(z);
		if(idNovogZapos == -1) {
			throw new MyException("Greska prilikom dodavanja novog zaposlenog.");
		}
		return ResponseEntity.ok("Uspesno sacuvani zaposleni, ciji je id: " +idNovogZapos + " i cija je pozicija: " + z.getUloga());
	}
	
	/*////

	OVAJ DEO JE ZA STOLOVE
	
	*/////
	
	@PostMapping("saveSto")
	public ResponseEntity<?> saveSto(@RequestParam int brs){
		if(brs <= 0) {
			throw new MyException("Broj stola mora biti veci od 0.");
		}
		Integer idNovogStola = service.saveSto(brs);
		if(idNovogStola == -1) {
			throw new MyException("Greska prilikom dodavanja novog stola.");
		}
		return ResponseEntity.ok("Uspesno sacuvani sto sa brojem" + brs + ", ciji je id: " +idNovogStola);
	}
	
				//get svi stolovi
	
	@GetMapping("getStoPoBroju")
	public ResponseEntity<?> getStoPoBroju(@RequestParam int brs){
		return ResponseEntity.status(HttpStatus.OK).body(service.getStoPoBroju(brs));
	}
	
	@GetMapping("getSlobodanSto")
	public ResponseEntity<?> getSlobodanSto(){
		return ResponseEntity.status(HttpStatus.OK).body(service.getSlobodniStolovi());
	}
	
	/*////

	OVAJ DEO JE ZA NARUDZBINE
	
	*/////

	@PostMapping("saveNarudzbina")
	public ResponseEntity<NarudzbinaDTO> saveNarudzbina(@RequestBody NarudzbinaDTO narudzbinaDTO){
		System.out.println(narudzbinaDTO);
		NarudzbinaDTO nova = service.saveNarudzbina(narudzbinaDTO);
		if(nova == null) {
			throw new MyException("Greska prilikom cuvanje nove narudzbine.");
		}
		System.out.println(nova);
		return ResponseEntity.ok(nova);
	}
	
	@GetMapping("getSveNarudzbine")
	public ResponseEntity<?> getSveNarudzbine(){
		return ResponseEntity.status(HttpStatus.OK).body(service.getSveNarudzbine());
	}

//	@GetMapping("getNeSpremneNarudzbine")
//	public ResponseEntity<?> getNeSpremneNarudzbine(){
//		return ResponseEntity.status(HttpStatus.OK).body(service.getNeSpremneNarudzbine());
//	}
	
	@GetMapping("setSpremnoToTrue/{id}")
    public ResponseEntity<Message> setSpremnoToTrue(@PathVariable int id) {
		System.out.println(id);
        service.setSpremnoToTrue(id);
        return ResponseEntity.ok(new Message("Status spremno je ažuriran na true za narudžbinu sa ID-jem: " + id));
    }
	
	/*
	
	OVAJ DEO JE ZA IZVESTAJ
	
	*/
	@RequestMapping(value = "/getLagerIzvestaj", method = RequestMethod.GET)
	public void getLagerIzvestaj(HttpServletResponse response) throws Exception {
		
		List<PiceDTO> pica = service.getSvaPica();
		
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(pica);
		InputStream inputStream = this.getClass().getResourceAsStream("/jasperreports/lager-izvestaj.jrxml");
		JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
		
		LocalDate datum = LocalDate.now();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("datum", datum);
		
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
		inputStream.close();
		
		response.setContentType("application/x-download");
		response.setHeader("Content-disposition", "attachment; fileName = IzvestajOLageru.pdf");
		
		OutputStream out = response.getOutputStream();
		JasperExportManager.exportReportToPdfStream(jasperPrint, out);
	}

}
