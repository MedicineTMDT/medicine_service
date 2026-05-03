package com.ryo.prescription.service.impl;

import com.ryo.prescription.dto.request.DrugRequest;
import com.ryo.prescription.dto.response.DrugResponse;
import com.ryo.prescription.dto.response.DrugSimpleResponse;
import com.ryo.prescription.entity.Category;
import com.ryo.prescription.entity.Drug;
import com.ryo.prescription.exception.AppException;
import com.ryo.prescription.exception.ErrorCode;
import com.ryo.prescription.projection.DrugProjection;
import com.ryo.prescription.repository.CategoryRepository;
import com.ryo.prescription.repository.DrugRepository;
import com.ryo.prescription.service.IDrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DrugServiceImpl implements IDrugService {

    private final DrugRepository drugRepository;
    private final CategoryRepository categoryRepository;

    private DrugResponse toDrugResponse(Drug drug) {
        return DrugResponse.builder()
                .id(drug.getId())
                .name(drug.getName())
                .slug(drug.getSlug())
                .metadata(drug.getMetadata())
                .build();
    }

    private DrugSimpleResponse toSimple(DrugProjection p) {
        return DrugSimpleResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .imageLink(
                        p.getImage() != null && !p.getImage().isEmpty()
                                ? p.getImage().getFirst()
                                : null
                )
                .build();
    }


    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public DrugResponse create(DrugRequest req) {
        if (drugRepository.findBySlug(req.getSlug()).isPresent())
            throw new AppException(ErrorCode.SLUG_EXISTS);
        if (drugRepository.findByName(req.getSlug()).isPresent())
            throw new AppException(ErrorCode.DRUG_EXISTS);

        Set<Category> categories = new HashSet<>();
        if (req.getCategoriesId() != null && !req.getCategoriesId().isEmpty()) {
            categories = new HashSet<>(
                    categoryRepository.findAllById(req.getCategoriesId())
            );

            // validate category có tồn tại đủ hay không
            if (categories.size() != req.getCategoriesId().size()) {
                throw new AppException(ErrorCode.NOT_FOUND);
            }
        }

        Drug drug = Drug.builder()
                .name(req.getName())
                .slug(req.getSlug())
                .content(req.getContent())
                .document(req.getDocument())
                .image(req.getImage())
                .ingredient(req.getIngredient())
                .metadata(req.getMetadata())
                .info(req.getInfo())
                .categories(categories)
                .build();

        return toDrugResponse(drugRepository.save(drug));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public DrugResponse update(DrugRequest req, Integer id) {
        Drug drug = drugRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DRUG_NOT_EXIST));

        drug.setName(req.getName());
        drug.setContent(req.getContent());
        drug.setDocument(req.getDocument());
        drug.setImage(req.getImage());
        drug.setIngredient(req.getIngredient());
        drug.setMetadata(req.getMetadata());
        drug.setInfo(req.getInfo());

        return toDrugResponse(drugRepository.save(drug));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Integer id) {
        drugRepository.deleteById(id);
    }

    @Override
    public Drug get(Integer id) {
        return drugRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DRUG_NOT_EXIST));
    }

    @Override
    public Page<DrugSimpleResponse> getAll(Pageable pageable) {
        return drugRepository.getAllProjectedBy(pageable).map(this::toSimple);
    }

    @Override
    public Page<DrugSimpleResponse> getAllByCategoryId(Pageable pageable, Integer id) {
        return drugRepository.findAllByCategories_Id(pageable, id)
                .map(this::toSimple);
    }

    @Override
    public Page<DrugSimpleResponse> getAllByDrugName(Pageable pageable, String name) {
        return drugRepository.findAllByNameContainingIgnoreCase(pageable, name)
                .map(this::toSimple);
    }

    @Override
    public List<String> getDrugIngredients(Integer drugId) {
        Drug drug = drugRepository.findById(drugId).orElseThrow(
                () -> new AppException(ErrorCode.DRUG_NOT_EXIST)
        );
        return drug.getIngredient();
    }

    @Override
    public List<DrugSimpleResponse> getTop10ByNameStartingWithIgnoreCase(String name) {
        return drugRepository.findTop10ByNameStartingWithIgnoreCase(name)
                .stream()
                .map(this::toSimple)
                .toList();
    }
}
