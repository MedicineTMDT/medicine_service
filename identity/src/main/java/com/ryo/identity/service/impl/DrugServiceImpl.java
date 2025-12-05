package com.ryo.identity.service.impl;

import com.ryo.identity.dto.request.DrugRequest;
import com.ryo.identity.dto.response.DrugResponse;
import com.ryo.identity.dto.response.DrugSimpleResponse;
import com.ryo.identity.entity.Category;
import com.ryo.identity.entity.Drug;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.exception.ErrorCode;
import com.ryo.identity.repository.CategoryRepository;
import com.ryo.identity.repository.DrugRepository;
import com.ryo.identity.service.IDrugService;
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

    private DrugSimpleResponse toSimple(Drug drug) {
        return DrugSimpleResponse.builder()
                .id(drug.getId())
                .name(drug.getName())
                .slug(drug.getSlug())
                .imageLink(drug.getImage() != null && !drug.getImage().isEmpty()
                        ? drug.getImage().getFirst()
                        : null)
                .build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
        return drugRepository.findAll(pageable).map(this::toSimple);
    }

    @Override
    public Page<DrugSimpleResponse> getAllByCategoryId(Pageable pageable, Integer id) {
        return drugRepository.findAllByCategoryId(pageable, id)
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
        return drug.getImage();
    }
}
