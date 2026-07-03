import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CriseDetailComponent } from './crise-detail.component';

describe('CriseDetailComponent', () => {
  let component: CriseDetailComponent;
  let fixture: ComponentFixture<CriseDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CriseDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CriseDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
