package nl.strohalm.cyclos.webservices.rest.ads.imports;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.ads.imports.ImportAdsForm;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.ads.imports.AdImport;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.ads.AdImportService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.csv.UnknownColumnException;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImportAdsController extends BaseRestController {
    
    private static class ImportAdsResponse extends GenericResponse {
        
        private List<Currency> currencies;
        private Currency singleCurrency;
        
        public Currency getSingleCurrency() {
            return singleCurrency;
        }
        
        public void setSingleCurrency(Currency singleCurrency) {
            this.singleCurrency = singleCurrency;
        }
        
        public List<Currency> getCurrencies() {
            return currencies;
        }
        
        public void setCurrencies(List<Currency> currencies) {
            this.currencies = currencies;
        }
        
    }
    
    public static class ImportAdsParameters {
        
        private Long id;
        private Long currency;
        private File file;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public Long getCurrency() {
            return currency;
        }
        
        public void setCurrency(Long currency) {
            this.currency = currency;
        }
        
        public File getFile() {
            return file;
        }
        
        public void setFile(File file) {
            this.file = file;
        }
        
    }
    
    @RequestMapping(value = "admin/importAds", headers = ("content-type=multipart/*"), method = RequestMethod.POST)
    @ResponseBody
    protected GenericResponse handleSubmit(@RequestBody ImportAdsParameters params, @RequestParam("file") MultipartFile inputFile) throws Exception {
        //final ImportAdsForm form = context.getForm();
        GenericResponse response = new GenericResponse();
//        final FormFile upload = form.getUpload();
//        if (upload == null || upload.getFileSize() == 0) {
//            throw new ValidationException("upload", "adImport.file",
//                    new RequiredError());
//        }
        AdImport adImport = new AdImport();
        adImport.setCurrency(currencyService.load(params.getCurrency()));

//        try {
//            adImport = adImportService.importAds(adImport,
//                    inputFile.getInputStream());
//            long importId = adImport.getId();
//            response.setImportId(importId);
//
//        } catch (final UnknownColumnException e) {
//            String err = "general.error.csv.unknownColumn";
//            response.setMessage(err);
//        } finally {
//            upload.destroy();
//        }
        HttpHeaders headers = new HttpHeaders();
        if (!inputFile.isEmpty()) {
            try {
//                String originalFilename = inputFile.getOriginalFilename();
//                File destinationFile = new File(context.getRealPath("/WEB-INF/uploaded") + File.separator + originalFilename);
//                inputFile.transferTo(destinationFile);
//                fileInfo.setFileName(destinationFile.getPath());
//                fileInfo.setFileSize(inputFile.getSize());
//                headers.add("File Uploaded Successfully - ", originalFilename);
//                return new ResponseEntity<FileInfo>(fileInfo, headers, HttpStatus.OK);
                adImport = adImportService.importAds(adImport,
                        inputFile.getInputStream());
                long importId = adImport.getId();
                // response.setImportId(importId);
            } catch (Exception e) {
                response.setMessage("HttpStatus.BAD_REQUEST");
            }
        } else {
            response.setMessage("file empty");
        }
        response.setMessage("file uploaded successfully!");
        response.setStatus(0);
        return response;
    }
    
    @RequestMapping(value = "admin/ImportAds", method = RequestMethod.GET)
    @ResponseBody
    public ImportAdsResponse prepareForm() throws Exception {
        ImportAdsResponse response = new ImportAdsResponse();
        
        final List<Currency> currencies = currencyService.listAll();
        if (currencies.size() == 1) {
            response.setSingleCurrency(currencies.get(0));
        }
        
        response.setCurrencies(currencies);
        response.setStatus(0);
        return response;
    }

//	protected void validateForm(final ActionContext context) {
//		final ImportAdsForm form = context.getForm();
//		final AdImport adImport = getDataBinder().readFromString(
//				form.getImport());
//		adImportService.validate(adImport);
//	}
}
