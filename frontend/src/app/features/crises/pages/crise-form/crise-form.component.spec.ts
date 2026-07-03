import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CriseFormComponent } from './crise-form.component';

describe('CriseFormComponent', () => {
  let component: CriseFormComponent;
  let fixture: ComponentFixture<CriseFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CriseFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CriseFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
